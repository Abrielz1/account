package ru.example.account.security.service.worker;

public interface BlacklistRefreshTokenQueryWorker {

    /**
     * Проверяет "горячий" (Redis) и "холодный" (Postgres) черные списки.
     * @param refreshToken "Сырой" refresh-токен.
     * @return true, если токен отозван.
     */
    boolean isRefreshTokenBlacklisted(String refreshToken);
}
