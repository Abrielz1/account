package ru.example.account.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.example.account.security.configuration.RedisKeysProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisKeyBuilderHelper {

    private final RedisKeysProperties redisKeys;

    public String buildKey(final String fingerprintHash) {
        return redisKeys.getKeys().getWhitelist().getFingerprintKeyFormat().replace("{fingerprint}", fingerprintHash);
    }
}
