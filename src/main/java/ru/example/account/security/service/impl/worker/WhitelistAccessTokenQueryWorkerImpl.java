package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.example.account.security.repository.BlacklistAccessTokenRepository;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.repository.RevokedSessionArchiveRepository;
import ru.example.account.security.repository.WhiteListAccessTokenRepository;
import ru.example.account.security.service.worker.WhitelistAccessTokenCommandWorker;
import ru.example.account.security.service.worker.WhitelistAccessTokenQueryWorker;
import ru.example.account.shared.util.RedisKeyBuilderHelper;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhitelistAccessTokenQueryWorkerImpl implements WhitelistAccessTokenQueryWorker {

    private static final String ERROR_NULL_OR_EMPTY_KEY_MESSAGE = "Key MUST NOT be NULL OR EMPTY!";

    private static final String REDIS_DOWN_MESSAGE = "REDIS IS DOWN! Could not ";

    private final RevokedSessionArchiveRepository revokedTokenArchiveRepository;

    private final BlacklistAccessTokenRepository blacklistAccessTokenRepository;

    private final WhiteListAccessTokenRepository whiteListAccessTokenRepository;

    private final RedisRepository<String, String> redisRepository;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;

    private final WhitelistAccessTokenCommandWorker whitelistAccessTokenCommandWorker;

    @Override
    public boolean isAccessTokenWhitelisted(String accessToken) {

        if (!StringUtils.hasText(accessToken)) {
            log.warn("[WARN] present refresh token " + ERROR_NULL_OR_EMPTY_KEY_MESSAGE);
            return false;
        }

        if (this.blacklistAccessTokenRepository.existsByToken(accessToken) ||
                this.revokedTokenArchiveRepository.existsByAccessToken(accessToken)
        ) {
            log.warn("[WARN] present access token ARE blacklisted!");
            // todo kafka send warning to security
            return false;
        }

        try {
            if (Boolean.TRUE.equals(redisRepository.exists(this.redisKeyBuilderHelper.buildRefreshKey(accessToken)))) {
                return true;
            }
        } catch (RedisConnectionFailureException e) {
            log.error(REDIS_DOWN_MESSAGE + "Blacklist check for refresh token will rely on Postgres.", e);
        }

        log.warn("White cache miss for refresh token. Checking Postgres refresh tokens archive.");

        if (this.whiteListAccessTokenRepository.existsByToken(accessToken)) {

            this.whitelistAccessTokenCommandWorker.whitelistRefreshToken(accessToken);
        }

        return false;
    }
}
