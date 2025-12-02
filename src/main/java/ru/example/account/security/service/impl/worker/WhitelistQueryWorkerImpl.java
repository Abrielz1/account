package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.repository.TrustedFingerprintRepository;
import ru.example.account.security.service.worker.WhitelistDeviceQueryWorker;
import ru.example.account.shared.util.RedisKeyBuilderHelper;
import ru.example.account.shared.util.WhitelistCacheWarmer;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhitelistQueryWorkerImpl implements WhitelistDeviceQueryWorker {

    private final RedisRepository<String, String> redisRepository;

    private final TrustedFingerprintRepository fingerprintRepository;

    private final JwtUtils jwtUtils;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;

    private final WhitelistCacheWarmer whitelistCacheWarmer;

    // Проверяем в обоих базах по хешу в токенах, наличие устройства в белом списке
    @Override
    @Transactional(value = "businessTransactionManager", readOnly = true)
    public boolean isDeviceTrusted(Long userId, String accessToken, String fingerprint) {

        if (userId == null || !StringUtils.hasText(accessToken) || !StringUtils.hasText(fingerprint)) {
            log.error("All given parameters MUST NOT be BLANC and NOT be a NULL!");
            return false;
        }

        final String key  = this.redisKeyBuilderHelper.buildKey(jwtUtils.createFingerprintHash(fingerprint));

        String accessTokenFromRedis = accessToken;
        try {
            if (this.redisRepository.exists(key)) {

                accessTokenFromRedis = this.redisRepository.findByKeyOrDefault(key, "");

                if (StringUtils.hasText(accessTokenFromRedis) && Objects.equals(accessToken, accessTokenFromRedis)) {

                    log.info("Given device is recognised");
                    return true;
                }
            }
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN! Whitelist check will rely on Postgres.", e);
        }

        if (this.fingerprintRepository.isFingerprintTrustedForUser(userId, fingerprint)) {

            this.whitelistCacheWarmer.warmUpCache(key, accessTokenFromRedis);

            return true;
        }

        log.error("Unknown device, Possible Security Breach!");
        return false;
    }
}
