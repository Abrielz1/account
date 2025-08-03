package ru.example.account.security.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.entity.TrustedFingerprint;
import ru.example.account.security.entity.UserFingerprintProfile;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.TrustedFingerprintRepository;
import ru.example.account.security.repository.UserFingerprintProfileRepository;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.SessionRevocationService;
import ru.example.account.security.service.TrustedDeviceService;
import ru.example.account.shared.exception.exceptions.FingerprintMissingException;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrustedDeviceServiceImpl implements TrustedDeviceService {

    // --- ВСЕ ЗАВИСИМОСТИ В ОДНОМ МЕСТЕ. ПРОСТО И ПОНЯТНО. ---
    private final StringRedisTemplate redisTemplate;
    private final RedisKeysProperties redisKeys;
    private final TrustedFingerprintRepository fingerprintRepository;
    private final UserFingerprintProfileRepository profileRepository;
    private final JwtUtils jwtUtils;
    private final SessionRevocationService sessionRevocationService;
    private final SessionQueryService sessionQueryService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional(value = "businessTransactionManager", readOnly = true)
    public boolean isDeviceTrusted(Long userId, String fingerprint, String refreshToken) {

        if (!StringUtils.hasText(fingerprint)) {
            log.trace("No finger print to check!");
            throw new FingerprintMissingException("No finger print to check!");
        }

        String fingerprintHash = this.jwtUtils.createFingerprintHash(fingerprint);

        // ЭШЕЛОН 1: РАБОТАЕМ С REDIS НАПРЯМУЮ
        try {
            String key = buildWhitelistRedisKey(fingerprintHash);
            String ownerIdInRedis = redisTemplate.opsForValue().get(key);
            String fingerPrintInDbToHash = this.sessionQueryService
                    .getFingerPrint(refreshToken).map(jwtUtils::createFingerprintHash)
                    .orElse(null);

            if (fingerPrintInDbToHash == null) {
                log.error("Security breached! A Hacker Attack! RED ALERT");
                sessionRevocationService.revokeAllSessionsForUser(userId,
                        SessionStatus.STATUS_COMPROMISED,
                        RevocationReason.REASON_RED_ALERT);
                throw new SecurityBreachAttemptException("Security breached! A Hacker Attack! RED ALERT");
            }

            if (!Objects.equals(fingerprintHash, fingerPrintInDbToHash)) {
                log.error("Security breached! A Hacker Attack! RED ALERT");
                sessionRevocationService.revokeAllSessionsForUser(userId,
                        SessionStatus.STATUS_COMPROMISED,
                        RevocationReason.REASON_RED_ALERT);
                throw new SecurityBreachAttemptException("Security breached! A Hacker Attack! RED ALERT");
            }

            if (ownerIdInRedis != null) {
                return ownerIdInRedis.equals(String.valueOf(userId));
            }
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN! Whitelist check will rely on Postgres.", e);
        }

        // ЭШЕЛОН 2: РАБОТАЕМ С POSTGRES НАПРЯМУЮ
        log.warn("Whitelist cache miss for user: {}. Checking Postgres.", userId);
        boolean isTrustedInDb = fingerprintRepository
                .isFingerprintTrustedForUser(userId, fingerprintHash);

        // ЭШЕЛОН 3: "ЛЕНИВЫЙ ПРОГРЕВ"
        if (isTrustedInDb) {
            log.info("Warming up whitelist Redis cache for user {} after Postgres lookup.", userId);
            warmUpWhitelistCache(userId, fingerprintHash);
        }

        return isTrustedInDb;
    }

    @Override
    @Transactional("businessTransactionManager")
    public void trustDevice(Long userId, String fingerprint, HttpServletRequest request) {
        String fingerprintHash = jwtUtils.createFingerprintHash(fingerprint);

        // 1. РАБОТАЕМ С POSTGRES НАПРЯМУЮ
        UserFingerprintProfile profile = profileRepository.findById(userId)
                .orElseGet(() -> createNewProfile(userId));
        profile.setLastUpdatedAt(ZonedDateTime.now());

        TrustedFingerprint trustedFp = findOrCreateTrustedFingerprint(profile, fingerprintHash);

        trustedFp.setLastSeenAt(ZonedDateTime.now());
        trustedFp.setIpAddress(request.getRemoteAddr());
        trustedFp.setUserAgent(request.getHeader("User-Agent"));
        trustedFp.setDeviceName("Unknown Device"); // TODO: В будущем - дать юзеру возможность именовать
        trustedFp.setTrusted(true);

        profileRepository.save(profile);
        log.info("Fingerprint for user {} has been trusted/updated in Postgres.", userId);

        // 2. РАБОТАЕМ С REDIS НАПРЯМУЮ ("Прогрев")
        warmUpWhitelistCache(userId, fingerprintHash);
    }

    @Override
    public String generateAndCacheVerificationCode(Long userId, String fingerprint) {
        String code = String.format("%06d", secureRandom.nextInt(999999));
        String key = buildVerificationRedisKey(userId, fingerprint);
        redisTemplate.opsForValue().set(key, code, redisKeys.getTtl().getDeviceVerificationCode());
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

    // =================================================================================
    //      ПРИВАТНЫЕ МЕТОДЫ-ХЕЛПЕРЫ ("ЧИСТЫЙ" КОД)
    // =================================================================================

    private void warmUpWhitelistCache(Long userId, String fingerprintHash) {
        try {
            String key = buildWhitelistRedisKey(fingerprintHash);
            redisTemplate.opsForValue().set(
                    key,
                    String.valueOf(userId),
                    redisKeys.getTtl().getTrustedFingerprint()
            );
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
        Optional<TrustedFingerprint> existingFp = profile.getTrustedFingerprints().stream()
                .filter(fp -> fp.getFingerprint().equals(fingerprintHash))
                .findFirst();

        if (existingFp.isPresent()) {
            return existingFp.get();
        } else {
            TrustedFingerprint newFp = new TrustedFingerprint();
            newFp.setFingerprint(fingerprintHash);
            newFp.setFirstSeenAt(ZonedDateTime.now());
            profile.addFingerprint(newFp);
            return newFp;
        }
    }

    private String buildWhitelistRedisKey(String fingerprintHash) {
        String keyFormat = redisKeys.getKeys().getWhitelist().getFingerprintKeyFormat();
        return keyFormat.replace("{fingerprint}", fingerprintHash);
    }

    private String buildVerificationRedisKey(Long userId, String fingerprint) {
        String fingerprintHash = jwtUtils.createFingerprintHash(fingerprint);
        String keyFormat = redisKeys.getKeys().getVerification().getDeviceCodeFormat();
        return keyFormat
                .replace("{userId}", String.valueOf(userId))
                .replace("{fingerprint}", fingerprintHash);
    }
}