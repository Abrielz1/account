package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import java.time.Instant;

public interface BlacklistService {

//    void addToBlacklist(Claims claims, Instant revocationTime, Duration duration);
//
//    boolean isBlacklisted(UUID sessionId);

    void blacklistAccessToken(String accessToken, Instant now);
    // Метод для refresh-токена
    void blacklistRefreshToken(String refreshToken, Instant now);

    // Методы-проверки
    boolean isAccessTokenBlacklisted(String accessToken);
    boolean isRefreshTokenBlacklisted(String refreshToken);

    void blacklistSessionTokens(AuthSession sessionToRevoke, Instant now);
}
