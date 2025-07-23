package ru.example.account.security.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.example.account.security.service.impl.AppUserDetails;

import java.time.ZonedDateTime;

public interface FingerprintService {

    String generateUsersFingerprint(HttpServletRequest request);

    Boolean isFingerPrintAreKnown(String fingerPrintToCheck);

    void save(String fingerprint, String ipAddress, String userAgent, ZonedDateTime lastSeenAt, Long userId);
}
