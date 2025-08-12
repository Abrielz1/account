package ru.example.account.security.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Сервис для управления "Белым Списком" доверенных устройств (фингерпринтов).
 * Отвечает за проверку, добавление и верификацию устройств.
 */
public interface WhitelistService {

    boolean isDeviceTrusted(final Long userId, final String accessToken, final String fingerprint);

    boolean trustDevice(final Long userId, final HttpServletRequest request, final String accessesToken);

    boolean unTrustDevice(String fingerprint);
}