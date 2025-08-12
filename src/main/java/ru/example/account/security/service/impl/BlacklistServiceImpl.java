package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.service.BlacklistService;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private static final String ERROR_NULL_OR_EMPTY_KEY_MESSAGE = "Key MUST NOT be NULL OR EMPTY!";

    private static final String REDIS_DOWN_MESSAGE = "REDIS IS DOWN! Could not ";

    private static final String KEY_STATUS_REVOKED = "revoked";

    private final RedisKeysProperties redisKeys;

    private final RevokedTokenArchiveRepository revokedTokenArchiveRepository;

    private final RedisRepository<String, String> redisRepository;

    // NB!
    // Spring автоматически найдет бин с именем "blacklistRedisRepository",
    // т.к. он единственный подходит под тип RedisRepository<String, String>.
    // Если бы бинов было несколько, мы бы использовали @Qualifier("blacklistRedisRepository").
    @Override
    public boolean isAccessTokenBlacklisted(String accessToken) {

        if (!StringUtils.hasText(accessToken)) {
            log.error(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
            return false;
        }
        try {
            // --- ЭШЕЛОН 1: REDIS ("Горячий" кеш) ---
            if (redisRepository.exists(this.buildAccessKey(accessToken))) {
                return true;
            }
        } catch (RedisConnectionFailureException e) {
            log.error(REDIS_DOWN_MESSAGE + "Blacklist check for refresh token will rely on Postgres.", e);
        }
        // --- ЭШЕЛОН 2: POSTGRES ("Холодный" архив) ---
        log.warn("Blacklist cache miss for access token. Checking Postgres archive.");
        if (revokedTokenArchiveRepository.existsByAccessToken(accessToken)) {
            // "Ленивый прогрев"
            this.blacklistAccessToken(accessToken);
            return true;
        }

        return false;
    }

    @Override
    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            log.error(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
            return false;
        }

        // --- ЭШЕЛОН 1: REDIS ---
        try {
            if (Boolean.TRUE.equals(redisRepository.exists(this.buildRefreshKey(refreshToken)))) {
                log.trace("Blacklisted refresh token found in Redis cache. Access denied.");
                return true;
            }
        } catch (RedisConnectionFailureException e) {
            log.error(REDIS_DOWN_MESSAGE + "Blacklist check for refresh token will rely on Postgres.", e);
        }

        // --- ЭШЕЛОН 2: POSTGRES (Архив) ---
        log.warn("Blacklist cache miss for refresh token. Checking Postgres archive.");
        if (revokedTokenArchiveRepository.existsByRefreshToken(refreshToken)) {
            // "Ленивый прогрев"
            log.info("Warming up refresh token blacklist cache after Postgres lookup.");
            this.blacklistRefreshToken(refreshToken);
            return true;
        }

        return false;
    }

    // --- Методы "поклажи" ("тупые" команды, которые только пишут в Redis) ---
    @Override
    public void blacklistAccessToken(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            log.error(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
            throw new IllegalArgumentException(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
        }

        try {
            this.redisRepository.save(
                    this.buildAccessKey(accessToken),
                    KEY_STATUS_REVOKED,
                    redisKeys.getTtl().getBannedAccessToken()
            );
        } catch (RedisConnectionFailureException exception) {
            log.error(REDIS_DOWN_MESSAGE + "blacklist access token.", exception);
        }
    }

    @Override
    public void blacklistRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            log.error(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
            throw new IllegalArgumentException(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
        }

        try {
            this.redisRepository.save(
                    this.buildRefreshKey(refreshToken),
                    KEY_STATUS_REVOKED,
                    redisKeys.getTtl().getBannedRefreshToken()
            );
        } catch (RedisConnectionFailureException exception) {
            log.error(REDIS_DOWN_MESSAGE + "blacklist access token.", exception);
        }
    }

    // тупые собиралки ключа для Redis
    private String buildAccessKey(String accessToken) {
        return redisKeys.getKeys().getBlacklist().getAccessTokenPrefix() + accessToken;
    }

    private String buildRefreshKey(String refreshToken) {
        return redisKeys.getKeys().getBlacklist().getRefreshTokenPrefix() + refreshToken;
    }

}
