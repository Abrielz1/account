package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.repository.ActiveSessionCacheRepository;
import ru.example.account.security.service.RedisSessionService;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisSessionServiceImpl implements RedisSessionService {

    private final ActiveSessionCacheRepository cacheRepository;

    @Value("${app.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
    public void cacheActiveSession(Long userId, UUID sessionId, String refreshToken, String accessToken, String fingerprintHash) {

        ActiveSessionCache redisSession = ActiveSessionCache.builder()
                .refreshTokenValue(refreshToken)
                .sessionId(sessionId)
                .userId(userId)
                .accessToken(accessToken)
                .fingerprintHash(fingerprintHash)
                .ttl(refreshTokenExpiration)
                .build();

        cacheRepository.save(redisSession);
    }
}
