package ru.example.account.security.service.todelete;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.service.AccessTokenBlacklistService;
import ru.example.account.security.service.impl.AppUserDetails;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;
import ru.example.account.shared.exception.exceptions.TokenRefreshException;
import ru.example.account.shared.util.FingerprintUtils;
import ru.example.account.user.entity.User;
import ru.example.account.user.repository.ClientRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final AuthSessionRepository authSessionRepository;

    private final ClientRepository userRepository;

    private final JwtUtils jwtUtils;

    private final AccessTokenBlacklistService blacklistService;

    private final SessionArchivingService sessionArchivingService; // Новый сервис для архивации

    private final SessionService sessionService;

    private final ApplicationEventPublisher eventPublisher;

    private final FingerprintUtils fingerprintUtils; // Получение слепка, не реализовано

    @Value("${app.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
    @Transactional("securityTransactionManager")
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        // Создаем и сохраняем новую сессию в Postgres
        String fingerprint = this.fingerprintUtils.generate(httpRequest); // Генерируем "отпечаток"
        AuthSession session = AuthSession.builder()
                .id(UUID.randomUUID()) // sessionId
                .userId(userDetails.getId())
                .refreshToken(UUID.randomUUID().toString())
                .status(SessionStatus.STATUS_ACTIVE)
                .fingerprintHash(fingerprint)
                .ipAddress(httpRequest.getRemoteAddr())
                .userAgent(httpRequest.getHeader("User-Agent"))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(refreshTokenExpiration))
                .build();
        authSessionRepository.save(session);
        log.info("New auth session created for user {}. Session ID: {}", userDetails.getId(), session.getId());

        // Генерируем токены
        String accessToken = jwtUtils.generateAccessToken(userDetails, session.getId());
        return new AuthResponse(userDetails.getId(),
                                            accessToken,
                                            session.getRefreshToken(),
                                            userDetails.getUsername(),
                                            new ArrayList<>()); // Дописать!
    }

    @Override
    @Transactional("securityTransactionManager")
    public AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String refreshTokenValue = request.tokenRefresh();

        // Ищем сессию по токену. Она должна быть ACTIVE.
        AuthSession session = authSessionRepository.findByRefreshTokenAndStatus(refreshTokenValue, SessionStatus.STATUS_ACTIVE)
                .orElseThrow(() -> new TokenRefreshException("Active session for this refresh token not found."));

        // Проверяем срок жизни
        if (session.getExpiresAt().isBefore(Instant.now())) {
            sessionArchivingService.archive(session, RevocationReason.REASON_EXPIRED);
            throw new TokenRefreshException("Refresh token has expired.");
        }

        // --- КРАСНАЯ ТРЕВОГА ---
        String currentFingerprint = this.fingerprintUtils.generate(httpRequest);
        if (!Objects.equals(session.getFingerprintHash(), currentFingerprint)) {
            session.setStatus(SessionStatus.STATUS_RED_ALERT);
            authSessionRepository.save(session);
            sessionArchivingService.archive(session, RevocationReason.REASON_RED_ALERT);
            // eventPublisher.publishEvent(...) // Публикуем событие для безопасников в микросервисе путём кафки
            throw new SecurityBreachAttemptException("Fingerprint mismatch. Session compromised.",
                    session.getUserId(), session.getId(), httpRequest.getRemoteAddr());
        }

        // --- Ротация токена ---
        // 1. Архивируем старую сессию
        sessionArchivingService.archive(session, RevocationReason.REASON_TOKEN_ROTATED);
        // 2. Создаем новую
        User user = userRepository.findById(session.getUserId()).orElseThrow();
        AppUserDetails userDetails = new AppUserDetails(user);

        return login(new LoginRequest(userDetails.getEmail(), null), httpRequest);
    }

    @Override
// Эта операция НЕ требует основной транзакции.
// Но для согласованности можно указать "securityTransactionManager".
    @Transactional("securityTransactionManager")
    public void logout(AppUserDetails userDetails) {
        UUID sessionId = userDetails.getSessionId();
        if (sessionId == null) {
            log.warn("Attempt to logout from a session without sessionId for user {}", userDetails.getId());
            return;
        }

        // Находим активную сессию
        authSessionRepository.findById(sessionId)
                .ifPresent(session -> {
                    // Архивируем ее с причиной LOGOUT
                    sessionArchivingService.archive(session, RevocationReason.REASON_USER_LOGOUT);

                    // Добавляем access_token во временный blacklist
                    // Для этого нам нужен сам токен. В UserDetails его нет.
                    // Это решается на уровне фильтра или контроллера, который имеет доступ к токену.
                    // Пока просто вызовем blacklist с sessionId.
                    // TTL должен быть равен остатку жизни access токена.
                    // Duration timeLeft = ...
                    // blacklistService.addToBlacklist(sessionId, timeLeft);
                    log.info("Session {} for user {} has been logged out.", sessionId, userDetails.getId());
                });
    }
}

