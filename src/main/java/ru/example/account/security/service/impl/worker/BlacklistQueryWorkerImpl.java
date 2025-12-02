package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.example.account.security.repository.BlacklistedAccessTokenRepository;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.repository.RevokedSessionArchiveRepository;
import ru.example.account.security.service.worker.BlacklistAccessTokenCommandWorker;
import ru.example.account.security.service.worker.BlacklistAccessTokenQueryWorker;
import ru.example.account.shared.util.RedisKeyBuilderHelper;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistQueryWorkerImpl implements BlacklistAccessTokenQueryWorker {

    private static final String ERROR_NULL_OR_EMPTY_KEY_MESSAGE = "Key MUST NOT be NULL OR EMPTY!";

    private static final String REDIS_DOWN_MESSAGE = "REDIS IS DOWN! Could not ";

    private final RevokedSessionArchiveRepository revokedTokenArchiveRepository;

    private final RedisRepository<String, String> redisRepository;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;

    private final BlacklistAccessTokenCommandWorker blacklistCommandWorker;

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
}
