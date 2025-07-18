package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.ActiveSessionCacheRepository;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.service.IdGenerationService;
import ru.example.account.security.service.SessionService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {


    private final AuthSessionRepository authSessionRepository;

    private final RevokedTokenArchiveRepository archiveRepository;

    private final ActiveSessionCacheRepository activeSessionCacheRepository;

    private final RedissonClient redissonClient;

    private final JwtUtils jwtUtils;

    private final IdGenerationService idGenerationService;

    @Value("${app.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
    public void createNewSession(AppUserDetails userDetails,
                                        String fingerprint,
                                        String ipAddress,
                                        String userAgent,
                                        String accessesToken,
                                        String refreshToken) {

     Instant currentTime = Instant.now();
        log.info("Persisting new AuthSession {} for user {}", userDetails.getSessionId(), userDetails.getId());

        AuthSession session =  AuthSession
                .builder()
                .id(this.idGenerationService.generateSessionId())
                .userId(userDetails.getId())
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .createdAt(currentTime)
                .expiresAt(currentTime.plus(refreshTokenExpiration))
                .status(SessionStatus.STATUS_ACTIVE)
                .accessToken(accessesToken)
                .refreshToken(refreshToken)
                .build();

        authSessionRepository.save(session);
    }

    @Override
    public Optional<AuthSession> findActiveByRefreshToken(String token) {
        return Optional.empty();
    }

    @Override
    public Optional<AuthSession> findById(UUID sessionId) {
        return Optional.empty();
    }

    @Override
    public void archive(AuthSession session, RevocationReason reason) {

    }

    @Override
    public void archiveAllForUser(Long userId, RevocationReason reason) {

    }
}
