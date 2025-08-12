package ru.example.account.security.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.entity.TrustedFingerprint;
import ru.example.account.security.entity.UserFingerprintProfile;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.repository.TrustedFingerprintRepository;
import ru.example.account.security.repository.UserFingerprintProfileRepository;
import ru.example.account.security.service.FingerprintService;
import ru.example.account.security.service.WhitelistService;
import ru.example.account.shared.exception.exceptions.FingerPrintNotFoundEception;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhitelistServiceImpl implements WhitelistService {

    private final RedisRepository<String, String> redisRepository;

    private final TrustedFingerprintRepository fingerprintRepository;

    private final UserFingerprintProfileRepository userFingerprintProfileRepository;

    private final RedisKeysProperties redisKeys;

    private final FingerprintService fingerprintService;

    private final JwtUtils jwtUtils;
    // Проверяем в обоих базах по хешу в токенах, наличие устройства в белом списке
    @Override
    @Transactional(value = "businessTransactionManager", readOnly = true)
    public boolean isDeviceTrusted(final Long userId, final String accessToken, final String fingerprint) {
        if (userId == null || !StringUtils.hasText(accessToken) || !StringUtils.hasText(fingerprint)) {
            log.error("All given parameters MUST NOT be BLANC and NOT be a NULL!");
            return false;
        }

        final String key  = this.buildKey(jwtUtils.createFingerprintHash(fingerprint));

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

            this.warmUpCache(key, accessTokenFromRedis);

            return true;
        }

        log.error("Unknown device, Possible Security Breach!");
        return false;
    }
    // Регистрируем устройство, снимаес устройства полный цифровой слепок. Кладём его в Redis по его хешу.
    @Override
    public boolean trustDevice(final Long userId, HttpServletRequest request, final String accessesToken) {

        final String newFingerPrint = this.fingerprintService.generateUsersFingerprint(request);
        final String newDeviceFingerPrintHash = this.jwtUtils.createFingerprintHash(newFingerPrint);

        UserFingerprintProfile newUserFingerprintProfile = this.userFingerprintProfileRepository.findByUserId(userId).orElseGet(() -> createNewProfile(userId));

        TrustedFingerprint newTrustedFingerprint = this.findOrCreateTrustedFingerprint(newUserFingerprintProfile, newFingerPrint);

        newTrustedFingerprint.setUserAgent(request.getHeader("User-Agent"));
        newTrustedFingerprint.setIpAddress(request.getRemoteAddr());
        newTrustedFingerprint.setVersion(0L);

            newUserFingerprintProfile.setVersion(0L);

        this.userFingerprintProfileRepository.save(newUserFingerprintProfile);
        log.info("Device for user {} has been trusted in Postgres.", userId);


        this.warmUpCache(newDeviceFingerPrintHash, accessesToken);

        return true;
    }

    @Override
    @Transactional(value = "businessTransactionManager")
    public boolean unTrustDevice(String fingerprint) {
        final String key = this.jwtUtils.createFingerprintHash(fingerprint);

        if (!redisRepository.exists(key)) {
            log.trace("");
            throw new IllegalArgumentException("");
        }

       this.redisRepository.delete(key);
       TrustedFingerprint trustedFingerprintFromDb = fingerprintRepository.findByFingerPrint(fingerprint).orElseThrow(() -> {
            log.error("No given trusted fingerprint registered in db");
            return new FingerPrintNotFoundEception("No given trusted fingerprint registered in db");
        });

       trustedFingerprintFromDb.setTrusted(false);
        return true;
    }

    private void warmUpCache(final String fingerprintHash, final String accessToken) {
        try {
            this.redisRepository.save(buildKey(fingerprintHash), accessToken, this.redisKeys.getTtl().getTrustedFingerprint());
            log.info("Whitelist cache warmed up for fingerprint: {}", fingerprintHash);
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN! Could not warm up whitelist cache.", e);
        }
    }

    private UserFingerprintProfile createNewProfile(final Long userId) {
        UserFingerprintProfile newProfile = new UserFingerprintProfile();
        newProfile.setUserId(userId);
        ZonedDateTime now = ZonedDateTime.now();
        newProfile.setCreatedAt(now);
        newProfile.setLastUpdatedAt(now);
        return newProfile;
    }

    private TrustedFingerprint findOrCreateTrustedFingerprint(final UserFingerprintProfile profile, final String fingerprint) {
        Optional<TrustedFingerprint> existingFp = profile.getTrustedFingerprints().stream()
                .filter(fp -> fp.getFingerprint().equals(fingerprint))
                .findFirst();

        if (existingFp.isPresent()) {
            return existingFp.get();
        } else {
            TrustedFingerprint newTrustedFingerprint = new TrustedFingerprint();
            newTrustedFingerprint.setTrusted(true);
            newTrustedFingerprint.setDeviceName("Unknown Device");
            newTrustedFingerprint.setFingerprint(fingerprint);
            newTrustedFingerprint.setFirstSeenAt(ZonedDateTime.now());
            profile.addFingerprint(newTrustedFingerprint); // Используем наш "чистый" метод-хелпер

            return newTrustedFingerprint;
        }
    }

    private String buildKey(final String fingerprintHash) {
        return redisKeys.getKeys().getWhitelist().getFingerprintKeyFormat().replace("{fingerprint}", fingerprintHash);
    }
}