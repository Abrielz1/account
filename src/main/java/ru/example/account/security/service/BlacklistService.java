package ru.example.account.security.service;

/**
 * Сервис для управления "Черным Списком" отозванных токенов.
 * Реализует "эшелонированную" проверку (Redis -> Postgres) и "ленивый прогрев" кеша.
 * "Источник Правды" для архива - RevokedSessionArchiveRepository.
 */
public interface BlacklistService {

    /**
     * ПРОВЕРЯЕТ, находится ли access-токен в черном списке.
     * @param accessToken "Сырой" access-токен.
     * @return true, если токен отозван.
     */
    boolean isAccessTokenBlacklisted(String accessToken);

    /**
     * ПРОВЕРЯЕТ, находится ли refresh-токен в черном списке.
     * @param refreshToken "Сырой" refresh-токен.
     * @return true, если токен отозван.
     */
    boolean isRefreshTokenBlacklisted(String refreshToken);

    /**
     * ДОБАВЛЯЕТ access-токен в "горячий" черный список Redis.
     * Вызывается из SessionRevocationService при отзыве сессии.
     */
    void blacklistAccessToken(String accessToken);

    /**
     * ДОБАВЛЯЕТ refresh-токен в "горячий" черный список Redis.
     */
    void blacklistRefreshToken(String refreshToken);
}