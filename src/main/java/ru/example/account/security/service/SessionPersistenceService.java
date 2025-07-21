package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.ActiveSessionCache;
import java.util.UUID;

public interface SessionPersistenceService {
    // Этот метод - атомарная операция запис
    AuthSession createAndSaveSession(UUID sessionId,
                                     Long userId,
                                     String fingerprint,
                                     String ipAddress,
                                     String userAgent,
                                     String accessToken,
                                     String refreshToken);

    // Этот метод - атомарная операция записи в Redis
    ActiveSessionCache createAndSaveActiveSessionCache(AuthSession session);

    // Этот метод - атомарная операция записи в Аудит
    void createAndSaveAuditLog(AuthSession session);
}