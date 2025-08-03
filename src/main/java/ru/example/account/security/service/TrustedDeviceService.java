package ru.example.account.security.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Сервис для управления "Белым Списком" доверенных устройств (фингерпринтов).
 * Отвечает за проверку, добавление и верификацию устройств.
 */
public interface TrustedDeviceService {

    /**
     * Проверяет, является ли фингерпринт доверенным для данного пользователя.
     * ОСНОВНАЯ ПРОВЕРКА ИДЕТ ЧЕРЕЗ БЫСТРЫЙ КЭШ (REDIS).
     * @param userId ID пользователя
     * @param fingerprint "Сырой" фингерпринт из запроса
     * @return true, если устройство доверенное
     */
    boolean isDeviceTrusted(Long userId, String fingerprint, String refreshToken);

    /**
     * Добавляет устройство в "доверенные" - и в Postgres, и в Redis-кеш.
     * Вызывается ПОСЛЕ успешной верификации (например, по коду из email).
     * @param userId ID пользователя
     * @param fingerprint "Сырой" фингерпринт
     * @param request HTTP-запрос для сбора доп. информации (IP, User-Agent)
     */
    void trustDevice(Long userId, String fingerprint, HttpServletRequest request);

    /**
     * Генерирует 6-значный код верификации и сохраняет его в Redis с коротким TTL.
     * @param userId ID пользователя
     * @param fingerprint "Сырой" фингерпринт
     * @return Сгенерированный код
     */
    String generateAndCacheVerificationCode(Long userId, String fingerprint);

    /**
     * Проверяет код, введенный пользователем, сопоставляя его с кодом из Redis.
     * При успехе - удаляет код из Redis.
     * @param userId ID пользователя
     * @param fingerprint "Сырой" фингерпринт
     * @param code Код от пользователя
     * @return true, если код верный
     */
    boolean verifyCode(Long userId, String fingerprint, String code);
}