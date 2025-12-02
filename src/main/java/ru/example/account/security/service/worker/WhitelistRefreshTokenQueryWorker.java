package ru.example.account.security.service.worker;

public interface WhitelistRefreshTokenQueryWorker {

   boolean isRefreshTokenWhitelisted(String refreshToken);
}
