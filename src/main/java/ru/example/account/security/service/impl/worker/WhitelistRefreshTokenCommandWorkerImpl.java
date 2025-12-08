package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.service.worker.WhitelistRefreshTokenCommandWorker;
import ru.example.account.shared.util.RedisKeyBuilderHelper;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhitelistRefreshTokenCommandWorkerImpl implements WhitelistRefreshTokenCommandWorker {

    private static final String ERROR_NULL_OR_EMPTY_KEY_MESSAGE = "Key MUST NOT be NULL OR EMPTY!";
    private static final String KEY_STATUS_WHITELISTED = "white_listed_refresh_token";
    private static final String REDIS_DOWN_MESSAGE = "REDIS IS DOWN! Could not ";

    private final RedisKeysProperties redisKeys;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;

    private final RedisRepository<String, String> redisRepository;
    @Override
    public void whitelistRefreshToken(String refreshToken) {

        if (!StringUtils.hasText(refreshToken)) {
            log.error(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
            throw new IllegalArgumentException(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
        }

        try {
            this.redisRepository.save(
                    this.redisKeyBuilderHelper.buildRefreshKey(refreshToken),
                    KEY_STATUS_WHITELISTED,
                    redisKeys.getTtl().getBannedRefreshToken()
            );
        } catch (RedisConnectionFailureException exception) {
            log.error(REDIS_DOWN_MESSAGE + "blacklist access token.", exception);
        }
    }

    @Override
    public void deWhiteListRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            log.error(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
            throw new IllegalArgumentException(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
        }

        try {
            this.redisRepository.delete(this.redisKeyBuilderHelper.buildRefreshKey(refreshToken));
        } catch (RedisConnectionFailureException exception) {
            log.error(REDIS_DOWN_MESSAGE + "blacklist access token.", exception);
        }
    }
}
