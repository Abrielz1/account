package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.service.IdGenerationService;
import ru.example.account.security.service.SessionPersistenceService;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.SessionRevocationService;
import ru.example.account.security.service.SessionServiceManager;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;
import ru.example.account.shared.exception.exceptions.TokenRefreshException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceManagerImpl implements SessionServiceManager {

    private final JwtUtils jwtUtils;

    private final IdGenerationService idGenerationService;

    private final SessionPersistenceService sessionPersistenceService;

    private final SessionQueryService sessionQueryService;

    private final SessionRevocationService sessionRevocationService;

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public AuthResponse createSession( AppUserDetails userDetails,
                                          String fingerprint,
                                          String ipAddress,
                                          String userAgent) {

        log.info("Creating new session for user ID: {}", userDetails.getId());

        // 1. Делегируем генерацию уникальных ID
        final UUID sessionId = this.idGenerationService.generateSessionId();
        final String refreshToken = this.idGenerationService.generateRefreshToken();
        // --- Шаг 2: Генерация refreshToken ---
        final String accessToken = this.jwtUtils.generateAccessToken(userDetails, sessionId);

        // --- Шаг 3: Создаем и сохраняем AuthSession в Postgres (наш журнал) ---
     AuthSession authSession =   this.sessionPersistenceService.createAndSaveSession(
                sessionId,
                userDetails.getId(),
                fingerprint,
                ipAddress,
                userAgent,
                accessToken,
                refreshToken);

        // --- Шаг 4: Создаем и сохраняем ActiveSessionCache в Redis ---
    ActiveSessionCache activeSessionCache = this.sessionPersistenceService.createAndSaveActiveSessionCache(authSession); // todo запихгуть в журнал сессий

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse rotateSessionAndTokens(String refreshToken,
                                               String accessesToken,
                                               String fingerPrint,
                                               AppUserDetails currentUser) {

        AuthSession sessionFromDb = this.sessionQueryService.findActiveByRefreshToken(refreshToken).orElseThrow(() -> {
            // Пытаются использовать несуществующий или уже отозванный refresh.
            // Возможно, это replay-атака.
            log.warn("SECURITY: Attempt to use a non-existent or revoked refresh token.");
            // eventPublisher.publishEvent(... "REPLAY_ATTACK" ...);
            return new TokenRefreshException("Refresh token is invalid.");
        });

        if (sessionFromDb.getExpiresAt().isBefore(Instant.now())) {
            // ... (архивируем с причиной EXPIRED и кидаем исключение)
        }

        if (!this.sessionQueryService.checkExistenceOfFingerprint(fingerPrint)) {
            // todo обработка вторжения
        }

        boolean isTokenBindingValid = Objects.equals(sessionFromDb.getAccessToken(), accessesToken);
        boolean isRefreshTokenValid = Objects.equals(sessionFromDb.getRefreshToken(), refreshToken);
        boolean isFingerprintValid = Objects.equals(sessionFromDb.getFingerprint(), fingerPrint);

        if (!(isTokenBindingValid && isRefreshTokenValid && isFingerprintValid)) {
            log.error("Hacker intrusion!. Red Alert!");
            // todo уведомление о атаке хакером
            // todo снести все сесси рефреш и акцесс токенов, зажурналить всю сессию и кинуть срочное уведомление на почту
            String reason = String.format(
                    "Security violation for session %s: TokenBindingOk=%b, RefreshTokenOk=%b, FingerprintOk=%b",
                    sessionFromDb.getId(), isTokenBindingValid, isRefreshTokenValid, isFingerprintValid
            );
            log.error("CRITICAL [SECURITY]: {}", reason);

            // ... (протокол "Красная тревога": отозвать все сессии, отправить алерт) ...
            sessionRevocationService.revokeAllSessionsForUser(sessionFromDb.getUserId(), RevocationReason.REASON_RED_ALERT);

            // todo обработка вторжения
            // Кидаем общее исключение, чтобы не давать атакующему подсказок.
            throw new SecurityBreachAttemptException("Security validation failed.");
        }
        // ==========================================================
        // ВСЕ СТРАЖНИКИ ПРОЙДЕНЫ. ЗАПРОС ЛЕГИТИМНЫЙ.
        // ==========================================================

        // ... (Штатная ротация, отзываем старую, создаем новую) ...

        return new AuthResponse(this.idGenerationService.generateRefreshToken(),
                this.jwtUtils.generateAccessToken(currentUser, sessionFromDb.getId()));
    }
}





