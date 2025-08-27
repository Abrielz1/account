package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.example.account.security.repository.BlacklistedAccessTokenRepository;
import ru.example.account.security.repository.BlacklistedRefreshTokenRepository;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.repository.RevokedSessionArchiveRepository;
import ru.example.account.security.service.worker.BlacklistCommandWorker;
import ru.example.account.security.service.worker.BlacklistQueryWorker;
import ru.example.account.shared.util.RedisKeyBuilderHelper;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistQueryWorkerImpl implements BlacklistQueryWorker {

    private static final String ERROR_NULL_OR_EMPTY_KEY_MESSAGE = "Key MUST NOT be NULL OR EMPTY!";

    private static final String REDIS_DOWN_MESSAGE = "REDIS IS DOWN! Could not ";

    private final RevokedSessionArchiveRepository revokedTokenArchiveRepository;

    private final RedisRepository<String, String> redisRepository;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;

    private final BlacklistCommandWorker blacklistCommandWorker;

    private final BlacklistedRefreshTokenRepository blacklistedRefreshTokenRepository;

    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;

    @Override
    public boolean isAccessTokenBlacklisted(String accessToken) {

        if (!StringUtils.hasText(accessToken)) {
            log.error(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
            return false;
        }
        try {
            // --- ЭШЕЛОН 1: REDIS ("Горячий" кеш) ---
            if (redisRepository.exists(this.redisKeyBuilderHelper.buildAccessKey(accessToken))) {
                return true;
            }
        } catch (RedisConnectionFailureException e) {
            log.error(REDIS_DOWN_MESSAGE + "Blacklist check for refresh token will rely on Postgres.", e);
        }

        // --- ЭШЕЛОН 2: POSTGRES ("Холодный" архив) ---
        log.warn("Blacklist cache miss for access token. Checking Postgres access tokens archive.");
        if (this.blacklistedAccessTokenRepository.existsByAccessToken(accessToken)) {
            // "Ленивый прогрев"
            this.blacklistCommandWorker.blacklistAccessToken(accessToken);
            return true;
        }

        log.warn("Blacklist cache miss for access token. Checking Postgres archive.");
        if (this.revokedTokenArchiveRepository.existsByAccessToken(accessToken)) {
            // "Ленивый прогрев"
            this.blacklistCommandWorker.blacklistAccessToken(accessToken);
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
            if (Boolean.TRUE.equals(redisRepository.exists(this.redisKeyBuilderHelper.buildRefreshKey(refreshToken)))) {
                log.trace("Blacklisted refresh token found in Redis cache. Access denied.");
                return true;
            }
        } catch (RedisConnectionFailureException e) {
            log.error(REDIS_DOWN_MESSAGE + "Blacklist check for refresh token will rely on Postgres.", e);
        }

        // --- ЭШЕЛОН 2: POSTGRES (Архив) ---

        log.warn("Blacklist cache miss for refresh token. Checking Postgres refresh tokens archive.");
        if (this.blacklistedRefreshTokenRepository.existsByRefreshToken(refreshToken)) {
            // "Ленивый прогрев"
            log.info("Warming up refresh token blacklist cache after Postgres lookup.");
            this.blacklistCommandWorker.blacklistRefreshToken(refreshToken);
            return true;
        }

        log.warn("Blacklist cache miss for refresh token. Checking Postgres archive.");
        if (this.revokedTokenArchiveRepository.existsByRefreshToken(refreshToken)) {
            // "Ленивый прогрев"
            log.info("Warming up refresh token blacklist cache after Postgres lookup.");
            this.blacklistCommandWorker.blacklistRefreshToken(refreshToken);
            return true;
        }

        return false;
    }
}
