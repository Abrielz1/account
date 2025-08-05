package ru.example.account.security.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Сервис для управления "Белым Списком" доверенных устройств (фингерпринтов).
 * Отвечает за проверку, добавление и верификацию устройств.
 */
public interface TrustedDeviceService {

    boolean isDeviceTrusted(Long userId, String fingerprint);
    void trustDevice(Long userId, String fingerprint, HttpServletRequest request);
    String generateAndCacheVerificationCode(Long userId, String fingerprint);
    boolean verifyCode(Long userId, String fingerprint, String code);
}