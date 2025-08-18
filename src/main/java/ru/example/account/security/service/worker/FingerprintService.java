package ru.example.account.security.service.worker;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;

public interface FingerprintService {

    String generateUsersFingerprint(HttpServletRequest request);

    Boolean isFingerPrintAreKnown(String fingerPrintToCheck);

    void save(String fingerprint, String ipAddress, String userAgent, ZonedDateTime lastSeenAt, Long userId);
}
