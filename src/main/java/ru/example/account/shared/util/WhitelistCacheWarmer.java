package ru.example.account.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.repository.RedisRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhitelistCacheWarmer {

    private final RedisRepository<String, String> redisRepository;

    private final RedisKeysProperties redisKeys;

    private final RedisKeyBuilderHelper keyBuilderHelper;

    public void warmUpCache(final String fingerprintHash, final String accessToken) {
        try {
            this.redisRepository.save(
                    this.keyBuilderHelper.buildKey(fingerprintHash),
                    accessToken,
                    this.redisKeys.getTtl().getTrustedFingerprint()
            );

            log.info("Whitelist cache warmed up for fingerprint: {}", fingerprintHash);
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN! Could not warm up whitelist cache.", e);
        }
    }
}
