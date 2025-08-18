package ru.example.account.security.service.impl.worker;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.TrustedFingerprint;
import ru.example.account.security.entity.UserFingerprintProfile;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.repository.TrustedFingerprintRepository;
import ru.example.account.security.repository.UserFingerprintProfileRepository;
import ru.example.account.security.service.worker.FingerprintService;
import ru.example.account.security.service.worker.WhitelistCommandWorker;
import ru.example.account.shared.exception.exceptions.FingerPrintNotFoundEception;
import ru.example.account.shared.util.FingerprintProfileHelper;
import ru.example.account.shared.util.WhitelistCacheWarmer;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhitelistCommandWorkerImpl implements WhitelistCommandWorker {

    private final RedisRepository<String, String> redisRepository;

    private final TrustedFingerprintRepository trustedFingerprintRepository;

    private final UserFingerprintProfileRepository userFingerprintProfileRepository;

    private final FingerprintService fingerprintService;

    private final FingerprintProfileHelper fingerprintProfileHelper;

    private final WhitelistCacheWarmer whitelistCacheWarmer;

    private final JwtUtils jwtUtils;
    // Проверяем в обоих базах по хешу в токенах, наличие устройства в белом списке

    @Override
    public boolean trustDevice(Long userId, HttpServletRequest request, String accessToken) {

        final String newFingerPrint = this.fingerprintService.generateUsersFingerprint(request);
        final String newDeviceFingerPrintHash = this.jwtUtils.createFingerprintHash(newFingerPrint);

        UserFingerprintProfile newUserFingerprintProfile = this.userFingerprintProfileRepository.findByUserId(userId)
                .orElseGet(() -> this.fingerprintProfileHelper.createNewProfile(userId)
                );

        TrustedFingerprint newTrustedFingerprint = this.fingerprintProfileHelper.findOrCreateTrustedFingerprint(
                newUserFingerprintProfile, newFingerPrint
        );

        newTrustedFingerprint.setUserAgent(request.getHeader("User-Agent"));
        newTrustedFingerprint.setIpAddress(request.getRemoteAddr());
        newTrustedFingerprint.setVersion(0L);

        newUserFingerprintProfile.setVersion(0L);

        this.userFingerprintProfileRepository.save(newUserFingerprintProfile);
        log.info("Device for user {} has been trusted in Postgres.", userId);


        this.whitelistCacheWarmer.warmUpCache(newDeviceFingerPrintHash, accessToken);

        return true;
    }

    @Override
    @Transactional(value = "businessTransactionManager")
    public boolean unTrustDevice(String fingerprint) {
        final String key = this.jwtUtils.createFingerprintHash(fingerprint);

        if (!this.redisRepository.exists(key)) {
            log.trace("");
            throw new IllegalArgumentException("");
        }

        this.redisRepository.delete(key);
        TrustedFingerprint trustedFingerprintFromDb = this.trustedFingerprintRepository.findByFingerPrint(fingerprint).orElseThrow(() -> {
            log.error("No given trusted fingerprint registered in db");
            return new FingerPrintNotFoundEception("No given trusted fingerprint registered in db");
        });

        trustedFingerprintFromDb.setTrusted(false);

        this.trustedFingerprintRepository.save(trustedFingerprintFromDb);
        return true;
    }
}
