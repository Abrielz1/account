package ru.example.account.security.service;

import ru.example.account.security.entity.RevocationReason;

public interface RevocationStrategy {

    boolean archiveAllForUser(Long userId, String fingerprint, String ipAddress, String userAgent, RevocationReason revocationReason);
}
