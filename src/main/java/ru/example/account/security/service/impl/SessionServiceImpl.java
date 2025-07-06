package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RefreshToken;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.RevokedTokenArchive;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RefreshToken;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RefreshTokenRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.service.SessionService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    // Репозитории для обеих баз
    private final RefreshTokenRepository refreshTokenRedisRepo; // Для Redis

    private final AuthSessionRepository authSessionPostgresRepo; // Для Postgres

    private final RevokedTokenArchiveRepository archivePostgresRepo; // для Postgres

    @Value("${app.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
    // Эта операция работает с ДВУМЯ хранилищами, но основное - Postgres.
    // Поэтому транзакция на security-базу.
    @Transactional("securityTransactionManager")
    public RefreshToken createSessionAndToken(Long userId, String fingerprintHash, String ipAddress, String userAgent) {

        // 1. Создаем персистентную запись о сессии в Postgres
        AuthSession session = AuthSession.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .status(SessionStatus.STATUS_ACTIVE)
                .fingerprintHash(fingerprintHash)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(refreshTokenExpiration))
                .build();
        authSessionPostgresRepo.save(session);

        // 2. Создаем "быструю" запись в Redis
        RefreshToken refreshToken = RefreshToken.builder()
                .sessionId(session.getId())
                .userId(userId)
                .ttl(refreshTokenExpiration)
                .build();

        return refreshTokenRedisRepo.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findActiveRefreshToken(String token) {
        // Просто ищем в Redis
        return refreshTokenRedisRepo.findByTokenRefresh(token);
    }

    @Override
    @Transactional(value = "securityTransactionManager", readOnly = true)
    public Optional<AuthSession> findSessionById(UUID sessionId) {
        return authSessionPostgresRepo.findById(sessionId);
    }

    @Override
    @Transactional("securityTransactionManager")
    public void archiveAndRevoke(RefreshToken token, RevocationReason reason) {
        // Архивируем и удаляем. Эта операция должна быть атомарной.

        // 1. Находим "главную" запись в Postgres, чтобы обновить ее статус
        authSessionPostgresRepo.findById(token.getSessionId()).ifPresent(session -> {
            session.setStatus(reason == RevocationReason.REASON_RED_ALERT ? SessionStatus.STATUS_RED_ALERT : SessionStatus.STATUS_REVOKED_BY_SYSTEM);
            authSessionPostgresRepo.save(session);

            // 2. Создаем запись в архиве
            RevokedTokenArchive archiveEntry = RevokedTokenArchive.builder()
                    .tokenValue(token.getTokenRefresh())
                    .sessionId(token.getSessionId())
                    .userId(token.getUserId())
                    .revokedAt(Instant.now())
                    .reason(reason)
                    .build();
            archivePostgresRepo.save(archiveEntry);

            // 3. Удаляем из Redis
            refreshTokenRedisRepo.delete(token);
        });
    }

    @Override
    @Transactional("securityTransactionManager")
    public void revokeAllForUser(Long userId, RevocationReason reason) {
        // 1. Находим все АКТИВНЫЕ сессии пользователя в Postgres
        List<AuthSession> activeSessions = authSessionPostgresRepo.findAllByUserIdAndStatus(userId, SessionStatus.STATUS_ACTIVE);

        // 2. Находим все АКТИВНЫЕ refresh-токены в Redis
        List<RefreshToken> activeRedisTokens = refreshTokenRedisRepo.findAllByUserId(userId);

        // 3. Для каждой активной сессии - архивируем и удаляем
        for (RefreshToken token : activeRedisTokens) {
            activeSessions.stream()
                    .filter(s -> s.getId().equals(token.getSessionId()))
                    .findFirst()
                    .ifPresent(s -> archiveAndRevoke(token, reason));
        }
    }
}
