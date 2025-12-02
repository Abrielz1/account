package ru.example.account.security.service.worker;

public interface BlacklistRefreshTokenCommandWorker {

    /**
     * ДОБАВЛЯЕТ refresh-токен в "горячий" черный список Redis.
     *
     * @param refreshToken "Сырой" refresh-токен.
     */
    void blacklistRefreshToken(String refreshToken);
}