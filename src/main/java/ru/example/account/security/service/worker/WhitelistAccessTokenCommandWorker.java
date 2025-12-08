package ru.example.account.security.service.worker;

public interface WhitelistAccessTokenCommandWorker {

    void whitelistRefreshToken(String refreshToken);

    void deWhiteListAccessToken(String accessToken);
}
