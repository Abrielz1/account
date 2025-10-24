package ru.example.account.security.service.impl.worker;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.ClientFingerPrintHistory;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.ClientFingerPrintHistoryRepository;
import ru.example.account.security.service.worker.FingerprintService;
import ru.example.account.shared.util.FingerprintUtils;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FingerprintServiceImpl implements FingerprintService {

    private final FingerprintUtils fingerprintUtils;

    private final JwtUtils jwtUtils;

    private final ClientFingerPrintHistoryRepository clientFingerPrintHistoryRepository;

    @Override
    public String generateUsersFingerprint(HttpServletRequest request) {

        return fingerprintUtils.generate(request);
    }

    @Override
    public Boolean isFingerPrintAreKnown(String fingerPrintHashToCheck) {
        return clientFingerPrintHistoryRepository.checkExistsClientFingerprint(fingerPrintHashToCheck);
    }

    @Override
    @Transactional("securityTransactionManager")
    public void save(String fingerprint, String ipAddress, String userAgent, ZonedDateTime lastSeenAt, Long userId) {

        ZonedDateTime now = ZonedDateTime.now();
        ClientFingerPrintHistory newHistory = ClientFingerPrintHistory.builder()
                .userId(userId)
                .fingerprintHash(this.jwtUtils.createFingerprintHash(fingerprint))
                .fingerprint(fingerprint)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .firstSeenAt(now)
                .lastSeenAt(now)
                .isTrusted(false)
                .build();

        clientFingerPrintHistoryRepository.save(newHistory);
    }
}
