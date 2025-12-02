package ru.example.account.security.service.impl.worker;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.TrustedFingerprint;
import ru.example.account.security.entity.UserFingerprintProfile;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.UserFingerprintProfileRepository;
import ru.example.account.security.service.worker.FingerprintService;
import ru.example.account.security.service.worker.WhitelistDeviceCommandWorker;
import ru.example.account.shared.util.FingerprintProfileHelper;
import ru.example.account.shared.util.WhitelistCacheWarmer;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhitelistDeviceCommandWorkerImpl implements WhitelistDeviceCommandWorker {

    private final UserFingerprintProfileRepository userFingerprintProfileRepository;

    private final FingerprintService fingerprintService;

    private final FingerprintProfileHelper fingerprintProfileHelper;

    private final WhitelistCacheWarmer whitelistCacheWarmer;

    private final JwtUtils jwtUtils;
    // Проверяем в обоих базах по хешу в токенах, наличие устройства в белом списке

    @Override
    @Transactional(value = "businessTransactionManager", propagation = Propagation.REQUIRES_NEW)
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
}
