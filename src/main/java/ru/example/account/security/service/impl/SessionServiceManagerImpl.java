package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.service.IdGenerationService;
import ru.example.account.security.service.RedisSessionService;
import ru.example.account.security.service.SessionService;
import ru.example.account.security.service.SessionServiceManager;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceManagerImpl implements SessionServiceManager {

    private final AuthSessionRepository authSessionRepository;

    private final SessionService sessionService; // Для Postgres

    private final RedisSessionService redisSessionService;

    private final JwtUtils jwtUtils;

    private final IdGenerationService idGenerationService;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
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
        this.sessionService.createNewSession(
                userDetails,
                fingerprint,
                ipAddress,
                userAgent,
                accessToken,
                refreshToken);
        // --- Шаг 4: Создаем и сохраняем ActiveSessionCache в Redis ---
        this.redisSessionService.cacheActiveSession(
                userDetails.getId(),
                sessionId,
                refreshToken,
                accessToken,
                passwordEncoder.encode(fingerprint));

        return new AuthResponse(accessToken, refreshToken);
    }
}
/*
AuthSession.builder()
                .id(sessionId)
                .userId(userDetails.getId())
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .fingerprintHash(fingerprint)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(SessionStatus.STATUS_ACTIVE)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(refreshTokenExpiration))
                .build();
 */