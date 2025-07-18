package ru.example.account.security.service;

import java.util.UUID;

public interface RedisSessionService {

    void cacheActiveSession(Long userId, UUID sessionId, String refreshToken, String accessToken, String fingerprint);
}
