package ru.example.account.security.service;

import ru.example.account.security.entity.enums.RevocationReason;

public interface RevocationStrategy { // todo под снос!

    boolean archiveAllForUser(Long userId, String fingerprint, String ipAddress, String userAgent, RevocationReason revocationReason);
}
