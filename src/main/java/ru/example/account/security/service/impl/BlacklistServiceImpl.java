package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.service.BlacklistService;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final RedisKeysProperties redisKeys;
    private final RevokedTokenArchiveRepository revokedTokenArchiveRepository;

    @Override
    public boolean isAccessTokenBlacklisted(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) return false;

        // --- ЭШЕЛОН 1: REDIS ---
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(buildAccessKey(accessToken)))) {
                return true;
            }
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN! Blacklist check for access token will rely on Postgres.", e);
        }

        // --- ЭШЕЛОН 2: POSTGRES (Архив) ---
        log.warn("Blacklist cache miss for access token. Checking Postgres archive.");
        if (revokedTokenArchiveRepository.existsByAccessToken(accessToken)) {
            // "Ленивый прогрев"
            warmUpAccessCache(accessToken);
            return true;
        }

        return false;
    }

    @Override
    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return false;

        // --- ЭШЕЛОН 1: REDIS ---
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(buildRefreshKey(refreshToken)))) {
                return true;
            }
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN! Blacklist check for refresh token will rely on Postgres.", e);
        }

        // --- ЭШЕЛОН 2: POSTGRES (Архив) ---
        log.warn("Blacklist cache miss for refresh token. Checking Postgres archive.");
        if (revokedTokenArchiveRepository.existsByRefreshToken(refreshToken)) {
            // "Ленивый прогрев"
            warmUpRefreshCache(refreshToken);
            return true;
        }

        return false;
    }

    // --- Методы "поклажи" ("тупые" команды, которые просто пишут в Redis) ---
    @Override
    public void blacklistAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) return;
        warmUpAccessCache(accessToken);
    }

    @Override
    public void blacklistRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        warmUpRefreshCache(refreshToken);
    }

    // --- Приватные хелперы ---

    private void warmUpAccessCache(String accessToken) {
        try {
            String key = buildAccessKey(accessToken);
            redisTemplate.opsForValue().set(key, "revoked", redisKeys.getTtl().getBannedAccessToken());
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN! Could not warm up access token blacklist cache.", e);
        }
    }

    private void warmUpRefreshCache(String refreshToken) {
        try {
            String key = buildRefreshKey(refreshToken);
            redisTemplate.opsForValue().set(key, "revoked", redisKeys.getTtl().getBannedRefreshToken());
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN! Could not warm up refresh token blacklist cache.", e);
        }
    }

    private String buildAccessKey(String accessToken) {
        return redisKeys.getKeys().getBlacklist().getAccessTokenPrefix() + accessToken;
    }

    private String buildRefreshKey(String refreshToken) {
        return redisKeys.getKeys().getBlacklist().getRefreshTokenPrefix() + refreshToken;
    }
}