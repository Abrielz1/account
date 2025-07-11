package ru.example.account.security.service;

import java.time.Duration;
import java.util.UUID;

public interface AccessTokenBlacklistService {

    void addToBlacklist(UUID sessionId, Duration duration);

    boolean isBlacklisted(UUID sessionId);
}
