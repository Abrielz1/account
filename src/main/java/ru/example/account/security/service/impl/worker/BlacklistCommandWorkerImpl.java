package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.service.worker.BlacklistCommandWorker;
import ru.example.account.shared.util.RedisKeyBuilderHelper;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistCommandWorkerImpl implements BlacklistCommandWorker {

    private static final String ERROR_NULL_OR_EMPTY_KEY_MESSAGE = "Key MUST NOT be NULL OR EMPTY!";

    private static final String REDIS_DOWN_MESSAGE = "REDIS IS DOWN! Could not ";

    private static final String KEY_STATUS_REVOKED = "revoked";

    private final RedisKeysProperties redisKeys;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;

    private final RedisRepository<String, String> redisRepository;

    @Override
    public void blacklistAccessToken(String accessToken) {

        if (!StringUtils.hasText(accessToken)) {
            log.error(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
            throw new IllegalArgumentException(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
        }

        try {
            this.redisRepository.save(
                    this.redisKeyBuilderHelper.buildAccessKey(accessToken),
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
                    this.redisKeyBuilderHelper.buildRefreshKey(refreshToken),
                    KEY_STATUS_REVOKED,
                    redisKeys.getTtl().getBannedRefreshToken()
            );
        } catch (RedisConnectionFailureException exception) {
            log.error(REDIS_DOWN_MESSAGE + "blacklist access token.", exception);
        }
    }
}
