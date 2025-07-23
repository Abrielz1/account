package ru.example.account.security.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.ClientFingerPrintHistory;
import ru.example.account.security.repository.ClientFingerPrintHistoryRepository;
import ru.example.account.security.service.FingerprintService;
import ru.example.account.shared.util.FingerprintUtils;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FingerprintServiceImpl implements FingerprintService {

    private final FingerprintUtils fingerprintUtils;

    private final ClientFingerPrintHistoryRepository clientFingerPrintHistoryRepository;

    @Override
    public String generateUsersFingerprint(HttpServletRequest request) {

        return fingerprintUtils.generate(request);
    }

    @Override
    public Boolean isFingerPrintAreKnown(String fingerPrintToCheck) {
        return clientFingerPrintHistoryRepository.checkExistsClientFingerprint(fingerPrintToCheck);
    }

    @Override
    public void save(String fingerprint, String ipAddress, String userAgent, ZonedDateTime lastSeenAt, Long userId) {

        ClientFingerPrintHistory newHistory = new ClientFingerPrintHistory();
// todo
        clientFingerPrintHistoryRepository.save(newHistory);
    }
}
