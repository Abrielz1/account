package ru.example.account.security.service;

import jakarta.servlet.http.HttpServletRequest;

public interface FingerprintService {

    String generateUsersFingerprint(HttpServletRequest request);
}
