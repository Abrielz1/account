package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.TrustedFingerprint;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.repository.TrustedFingerprintRepository;
import ru.example.account.security.service.worker.BlacklistDeviceCommandWorker;
import ru.example.account.shared.exception.exceptions.FingerPrintNotFoundEception;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlacklistDeviceCommandWorkerImpl implements BlacklistDeviceCommandWorker {

    private final RedisRepository<String, String> redisRepository;

    private final TrustedFingerprintRepository trustedFingerprintRepository;

    private final JwtUtils jwtUtils;
    // Проверяем в обоих базах по хешу в токенах, наличие устройства в белом списке

    @Override
    @Transactional(value = "businessTransactionManager", propagation = Propagation.REQUIRES_NEW)
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
