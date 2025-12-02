package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.BlacklistedAccessToken;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.repository.BlacklistAccessTokenRepository;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.service.worker.BlacklistAccessTokenCommandWorker;
import ru.example.account.shared.util.RedisKeyBuilderHelper;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistAccessTokenCommandWorkerImpl implements BlacklistAccessTokenCommandWorker {

    private static final String ERROR_NULL_OR_EMPTY_KEY_MESSAGE = "Key MUST NOT be NULL OR EMPTY!";

    private static final String REDIS_DOWN_MESSAGE = "REDIS IS DOWN! Could not ";

    private static final String KEY_STATUS_REVOKED = "revoked";

    private final RedisKeysProperties redisKeys;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;

    private final RedisRepository<String, String> redisRepository;

    private final BlacklistAccessTokenRepository blacklistAccessTokenRepository;

    @Override
    @Transactional()
    public void blacklistAccessToken(AuthSession currentSession, RevocationReason reason) {

        if (!StringUtils.hasText(currentSession.getAccessToken())) {
            log.error(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
            throw new IllegalArgumentException(ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
        }

        try {
            this.redisRepository.save(
                    this.redisKeyBuilderHelper.buildAccessKey(currentSession.getAccessToken()),
                    KEY_STATUS_REVOKED,
                    redisKeys.getTtl().getBannedAccessToken()
            );

            BlacklistedAccessToken accessTokenToBlackList = new BlacklistedAccessToken();

            accessTokenToBlackList.setUp(currentSession, Instant.now(), reason);

            this.blacklistAccessTokenRepository.save(accessTokenToBlackList);
        } catch (RedisConnectionFailureException exception) {
            log.error(REDIS_DOWN_MESSAGE + "blacklist access token.", exception);
        }
    }

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
}
