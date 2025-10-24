package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.principal.AppUserDetails;
import ru.example.account.security.service.facade.MailSendService;
import ru.example.account.security.service.RevocationStrategy;
import ru.example.account.security.service.SessionPersistenceService;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.SessionServiceManager;
import ru.example.account.security.service.impl.facede.SessionRevocationServiceImpl;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;
import ru.example.account.shared.exception.exceptions.SessionExpiredException;
import ru.example.account.shared.exception.exceptions.TokenRefreshException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceManagerImpl implements SessionServiceManager {

    private final SessionPersistenceService sessionPersistenceService;

    private final SessionQueryService sessionQueryService;

    private final RevocationStrategy sessionCommandService;

    private final SessionRevocationServiceImpl sessionRevocationService;

    private final MailSendService mailSendService;

    private final JwtUtils jwtUtils;

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public AuthResponse createSession(AppUserDetails currentUser,
                                      String ipAddress,
                                      String fingerprint,
                                      String userAgent,
                                      ZonedDateTime lastSeenAt) {

        log.info("Creating new session for user ID: {}", currentUser.getId());

        if (!this.sessionQueryService.checkExistenceOfFingerprint(this.jwtUtils.createFingerprintHash(fingerprint))) {
            log.trace("someone with unknown fingerprint attempt to login! ip: {}, fingerprint: {}, useragent: {}, userId: {}, email: {}",
                    ipAddress, fingerprint, userAgent, currentUser.getId(), currentUser.getEmail());

            mailSendService.sendAlertMail(fingerprint, ipAddress, userAgent, lastSeenAt, currentUser.getId());
            this.sessionPersistenceService.saveFingerPrint(fingerprint, ipAddress, userAgent, lastSeenAt, currentUser.getId());
        }

        // --- Шаг 3: Создаем и сохраняем AuthSession в Postgres (наш журнал) ---
        AuthSession newAuthSession =   this.sessionPersistenceService.createAndSaveSession(
                currentUser,
                fingerprint,
                ipAddress,
                userAgent);

        // --- Шаг 4 и 5: Создаем и сохраняем ActiveSessionCache в Redis и навсегда в Postgres---
        this.sessionPersistenceService.createAndSaveActiveSessionCache(newAuthSession, currentUser);
        this.sessionPersistenceService.createAndSaveAuditLog(newAuthSession);

        return new AuthResponse(newAuthSession.getAccessToken(), newAuthSession.getRefreshToken());
    }

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public AuthResponse rotateSessionAndTokens(String refreshToken,
                                               String accessesToken,
                                               String fingerprint,
                                               String ipAddress,
                                               String userAgent,
                                               AppUserDetails currentUser) {

        AuthSession sessionFromDb = this.sessionQueryService.findByRefreshTokenAndStatus(refreshToken).orElseThrow(() -> {
            // Пытаются использовать несуществующий или уже отозванный refresh.
            // Возможно, это replay-атака.
            log.warn("SECURITY: Attempt to use a non-existent or revoked refresh token.");

            if (sessionQueryService.isTokenArchived(refreshToken)) {
                this.sessionRevocationService.revokeAllSessionsForUser(currentUser.getId(), SessionStatus.STATUS_COMPROMISED, RevocationReason.REASON_RED_ALERT);
                this.mailSendService.sendReplayAttackNotification(refreshToken,
                        accessesToken,
                        fingerprint,
                        ipAddress,
                        userAgent,
                        currentUser);
            }

            return new TokenRefreshException("Refresh token is invalid.");
        });

        if (sessionFromDb.getExpiresAt().isBefore(Instant.now())) {
            // ... (архивируем с причиной EXPIRED и кидаем исключение)
            log.trace("Token is outdated!");
            this.sessionRevocationService.revokeAndArchive(sessionFromDb, SessionStatus.STATUS_COMPROMISED, RevocationReason.REASON_EXPIRED);
            throw new SessionExpiredException("Token is outdated!");
        }

        boolean isTokenBindingValid = Objects.equals(sessionFromDb.getAccessToken(), accessesToken);
        boolean isRefreshTokenValid = Objects.equals(sessionFromDb.getRefreshToken(), refreshToken);
        boolean isFingerprintValid = Objects.equals(sessionFromDb.getFingerprint(), fingerprint);
        boolean isCompromisedSession = Objects.equals(sessionFromDb.getStatus(), SessionStatus.STATUS_COMPROMISED);

        if (isCompromisedSession) {

            log.error("Hacker intrusion!. Red Alert!");

            String reason = String.format(
                    "Security violation for session %s: TokenBindingOk=%b, RefreshTokenOk=%b, FingerprintOk=%b, because Session status is: %s",
                    sessionFromDb.getId(), isTokenBindingValid, isRefreshTokenValid, isFingerprintValid, sessionFromDb.getStatus()
            );

            log.error("CRITICAL [SECURITY]: {}", reason);

            // ... (протокол "Красная тревога": сдампить всю сессию,  отозвать все сессии, отправить алерт) ...
            this.sessionCommandService.archiveAllForUser(sessionFromDb.getUserId(), fingerprint, ipAddress, userAgent, RevocationReason.REASON_RED_ALERT);

            // шлём срочное уведомление об хакерской атаке
            this.mailSendService.sendRedAlertNotification(sessionFromDb.getUserId(), fingerprint, ipAddress, userAgent, currentUser, RevocationReason.REASON_RED_ALERT);

            // Кидаем общее исключение, чтобы не давать атакующему подсказок.
            throw new SecurityBreachAttemptException("Security validation failed.");
        }

        if (!(isTokenBindingValid && isRefreshTokenValid && isFingerprintValid)) {
            log.error("Hacker intrusion!. Red Alert!");

            String reason = String.format(
                    "Security violation for session %s: TokenBindingOk=%b, RefreshTokenOk=%b, FingerprintOk=%b",
                    sessionFromDb.getId(), isTokenBindingValid, isRefreshTokenValid, isFingerprintValid
            );

            log.error("CRITICAL [SECURITY]: {}", reason);

            // ... (протокол "Красная тревога": сдампить всю сессию, отозвать все сессии, отправить алерт) ...
            this.sessionCommandService.archiveAllForUser(sessionFromDb.getUserId(), fingerprint, ipAddress, userAgent, RevocationReason.REASON_RED_ALERT);

            // шлём срочное уведомление об хакерской атаке
            this.mailSendService.sendRedAlertNotification(sessionFromDb.getUserId(), fingerprint, ipAddress, userAgent, currentUser, RevocationReason.REASON_RED_ALERT);

            // Кидаем общее исключение, чтобы не давать атакующему подсказок.
            throw new SecurityBreachAttemptException("Security validation failed.");
        }

        // ==========================================================
        // ВСЕ СТРАЖНИКИ ПРОЙДЕНЫ. ЗАПРОС ЛЕГИТИМНЫЙ.
        // ==========================================================

        // ... (Штатная ротация, отзываем старую, создаем новую) ..

         // 3. Архивируем и отзываем старую сессию
          this.sessionRevocationService.revokeAndArchive(sessionFromDb, SessionStatus.STATUS_REVOKED_BY_USER, RevocationReason.REASON_TOKEN_ROTATED);

        // 4. СОЗДАЕМ НОВУЮ СЕССИЮ (переиспользуем логику из `createSession`)
        //    Это предполагает, что в `createSession` ты уже вынес логику `isNewDevice`.
        //    Если мы хотим оставить `createSession` "тупым", то просто создаем сессию здесь.


        AuthSession newAuthSession = this.sessionPersistenceService.createAndSaveSession(currentUser,
                                                                                         fingerprint,
                                                                                         ipAddress,
                                                                                         userAgent);

        sessionPersistenceService.createAndSaveActiveSessionCache(newAuthSession, currentUser);
        sessionPersistenceService.createAndSaveAuditLog(newAuthSession);

        log.info("Client with id: {} session successfully created!", currentUser.getId());

        return new AuthResponse(newAuthSession.getAccessToken(),
                                newAuthSession.getRefreshToken());
    }

    @Override
    public void logout(AppUserDetails userToLogOut) {

        sessionQueryService.findById(userToLogOut.getSessionId())
                .ifPresent(sessionToRevoke -> sessionRevocationService.revokeAndArchive(sessionToRevoke,
                        SessionStatus.STATUS_REVOKED_BY_USER,
                        RevocationReason.REASON_USER_LOGOUT));
    }

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void logoutAll(AppUserDetails userToLogOut) {

        this.sessionRevocationService.revokeAllSessionsForUser(userToLogOut.getId(),
                SessionStatus.STATUS_POTENTIAL_COMPROMISED,
                RevocationReason.REASON_REVOKED_BY_USER_ON_ALL_DEVICES_SECURITY_ATTENTION);
    }
}