package ru.example.account.security.service.worker;

public interface WhitelistAccessTokenQueryWorker {

    boolean isAccessTokenWhitelisted(String accessToken);
}
