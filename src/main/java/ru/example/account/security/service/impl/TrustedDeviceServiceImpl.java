package ru.example.account.security.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.entity.TrustedFingerprint;
import ru.example.account.security.entity.UserFingerprintProfile;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.TrustedFingerprintRepository;
import ru.example.account.security.repository.UserFingerprintProfileRepository;
import ru.example.account.security.service.TrustedDeviceService;
import java.security.SecureRandom;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrustedDeviceServiceImpl implements TrustedDeviceService {

    private final StringRedisTemplate redisTemplate;
    private final RedisKeysProperties redisKeys;
    private final TrustedFingerprintRepository fingerprintRepository;
    private final UserFingerprintProfileRepository profileRepository;
    private final JwtUtils jwtUtils;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional(value = "businessTransactionManager", readOnly = true)
    public boolean isDeviceTrusted(Long userId, String fingerprint) {
        String fingerprintHash = jwtUtils.createFingerprintHash(fingerprint);
        try {
            if (this.isTrustedInRedis(userId, fingerprintHash)) return true;
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN! Whitelist check will rely on Postgres.", e);
        }
        if (fingerprintRepository.isFingerprintTrustedForUser(userId, fingerprintHash)) {
            this.warmUpWhitelistCache(userId, fingerprintHash);
            return true;
        }
        return false;
    }

    @Override
    @Transactional("businessTransactionManager")
    public void trustDevice(Long userId, String fingerprint, HttpServletRequest request) {
        String fingerprintHash = jwtUtils.createFingerprintHash(fingerprint);
        UserFingerprintProfile profile = profileRepository.findById(userId).orElseGet(() -> createNewProfile(userId));
        profile.setLastUpdatedAt(ZonedDateTime.now());
        TrustedFingerprint trustedFp = findOrCreateTrustedFingerprint(profile, fingerprintHash);
        trustedFp.setLastSeenAt(ZonedDateTime.now());
        trustedFp.setIpAddress(request.getRemoteAddr());
        trustedFp.setUserAgent(request.getHeader("User-Agent"));
        trustedFp.setDeviceName("Unknown Device");
        trustedFp.setTrusted(true);
        this.profileRepository.save(profile);
        log.info("Fingerprint for user {} has been trusted/updated in Postgres.", userId);
        warmUpWhitelistCache(userId, fingerprintHash);
    }

    @Override
    public String generateAndCacheVerificationCode(Long userId, String fingerprint) {
        String code = String.format("%06d", secureRandom.nextInt(999999));
        String key = buildVerificationRedisKey(userId, fingerprint);
        this.redisTemplate.opsForValue().set(key, code, redisKeys.getTtl().getDeviceVerificationCode());
        return code;
    }

    @Override
    public boolean verifyCode(Long userId, String fingerprint, String code) {
        String key = buildVerificationRedisKey(userId, fingerprint);
        String storedCode = redisTemplate.opsForValue().get(key);
        if (code != null && code.equals(storedCode)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private boolean isTrustedInRedis(Long userId, String fingerprintHash) {
        String key = buildWhitelistRedisKey(fingerprintHash);
        String ownerIdInRedis = redisTemplate.opsForValue().get(key);
        return ownerIdInRedis != null && ownerIdInRedis.equals(String.valueOf(userId));
    }

    private void warmUpWhitelistCache(Long userId, String fingerprintHash) {
        try {
            String key = buildWhitelistRedisKey(fingerprintHash);
            redisTemplate.opsForValue().set(key, String.valueOf(userId), redisKeys.getTtl().getTrustedFingerprint());
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS STILL DOWN! Could not warm up whitelist cache for user {}.", userId, e);
        }
    }

    private UserFingerprintProfile createNewProfile(Long userId) {
        UserFingerprintProfile newProfile = new UserFingerprintProfile();
        newProfile.setUserId(userId);
        newProfile.setCreatedAt(ZonedDateTime.now());
        return newProfile;
    }

    private TrustedFingerprint findOrCreateTrustedFingerprint(UserFingerprintProfile profile, String fingerprintHash) {
        return profile.getTrustedFingerprints().stream()
                .filter(fp -> fp.getFingerprint().equals(fingerprintHash))
                .findFirst()
                .orElseGet(() -> {
                    TrustedFingerprint newFp = new TrustedFingerprint();
                    newFp.setFingerprint(fingerprintHash);
                    newFp.setFirstSeenAt(ZonedDateTime.now());
                    profile.addFingerprint(newFp);
                    return newFp;
                });
    }

    private String buildWhitelistRedisKey(String fingerprintHash) {
        return this.redisKeys.getKeys().getWhitelist().getFingerprintKeyFormat().replace("{fingerprint}", fingerprintHash);
    }

    private String buildVerificationRedisKey(Long userId, String fingerprint) {
        String fingerprintHash = jwtUtils.createFingerprintHash(fingerprint);
        String keyFormat = redisKeys.getKeys().getVerification().getDeviceCodeFormat();
        return keyFormat.replace("{userId}", String.valueOf(userId)).replace("{fingerprint}", fingerprintHash);
    }
}