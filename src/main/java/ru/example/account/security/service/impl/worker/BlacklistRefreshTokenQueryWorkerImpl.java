package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.example.account.security.repository.BlacklistedRefreshTokenRepository;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.repository.RevokedSessionArchiveRepository;
import ru.example.account.security.service.worker.BlacklistRefreshTokenCommandWorker;
import ru.example.account.security.service.worker.BlacklistRefreshTokenQueryWorker;
import ru.example.account.shared.util.RedisKeyBuilderHelper;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistRefreshTokenQueryWorkerImpl implements BlacklistRefreshTokenQueryWorker {

    private static final String ERROR_NULL_OR_EMPTY_KEY_MESSAGE = "Key MUST NOT be NULL OR EMPTY!";

    private static final String REDIS_DOWN_MESSAGE = "REDIS IS DOWN! Could not ";

    private final RevokedSessionArchiveRepository revokedTokenArchiveRepository;

    private final RedisRepository<String, String> redisRepository;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;

    private final BlacklistedRefreshTokenRepository blacklistedRefreshTokenRepository;

    private final BlacklistRefreshTokenCommandWorker blacklistRefreshTokenCommandWorker;

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
            this.blacklistRefreshTokenCommandWorker.blacklistRefreshToken(refreshToken);
            return true;
        }

        log.warn("Blacklist cache miss for refresh token. Checking Postgres archive.");
        if (this.revokedTokenArchiveRepository.existsByRefreshToken(refreshToken)) {
            // "Ленивый прогрев"
            log.info("Warming up refresh token blacklist cache after Postgres lookup.");
            this.blacklistRefreshTokenCommandWorker.blacklistRefreshToken(refreshToken);
            return true;
        }

        return false;
    }
}
