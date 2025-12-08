package ru.example.account.security.service.worker;

public interface WhitelistRefreshTokenCommandWorker {

    void whitelistRefreshToken(String refreshToken);

    void deWhiteListRefreshToken(String refreshToken);
}
