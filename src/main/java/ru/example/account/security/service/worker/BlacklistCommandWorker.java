package ru.example.account.security.service.worker;

/**
 * COMMAND-ВОРКЕР.
 * Умеет ТОЛЬКО, добавлять токены в "горячий" черный список Redis.
 * Он, НИЧЕГО не знает о том, как их "проверять".
 */
public interface BlacklistCommandWorker {

    /**
     * ДОБАВЛЯЕТ access-токен в "горячий" черный список Redis.
     * Не возвращает ничего (void), потому что это "fire-and-forget" команда.
     * Если не получится - он, залогирует, но не уронит весь процесс.
     *
     * @param accessToken "Сырой" access-токен.
     */
    void blacklistAccessToken(String accessToken);

    /**
     * ДОБАВЛЯЕТ refresh-токен в "горячий" черный список Redis.
     *
     * @param refreshToken "Сырой" refresh-токен.
     */
    void blacklistRefreshToken(String refreshToken);
}
