package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.repository.ActiveSessionCacheRepository;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.service.SessionPersistenceService;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionPersistenceServiceImpl implements SessionPersistenceService {

    private final AuthSessionRepository authSessionRepository;

    private final ActiveSessionCacheRepository cacheRepository;

    @Value("${app.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
    @Transactional(value = "securityTransactionManager")
    public AuthSession createAndSaveSession(UUID sessionId,
                                            Long userId,
                                            String fingerprint,
                                            String ipAddress,
                                            String userAgent,
                                            String accessToken,
                                            String refreshToken) {


        Instant currentTime = Instant.now();
        log.info("Persisting new AuthSession {} for user {}", sessionId, userId);

        AuthSession session =  AuthSession
                .builder()
                .id(sessionId)
                .userId(userId)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .createdAt(currentTime)
                .expiresAt(currentTime.plus(refreshTokenExpiration))
                .status(SessionStatus.STATUS_ACTIVE)
                .fingerprint(fingerprint)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return  authSessionRepository.save(session);
    }

    @Override
    public ActiveSessionCache createAndSaveActiveSessionCache(AuthSession session) {
        ActiveSessionCache redisSession = ActiveSessionCache.builder()
                .refreshToken(session.getRefreshToken())
                .sessionId(session.getId())
                .userId(session.getUserId())
                .accessToken(session.getAccessToken())
                .fingerprint(session.getFingerprint())
                .ttl(refreshTokenExpiration)
                .build();

      return cacheRepository.save(redisSession);
    }

    @Override
    public void createAndSaveAuditLog(AuthSession session) {
    // todo
    }
}
