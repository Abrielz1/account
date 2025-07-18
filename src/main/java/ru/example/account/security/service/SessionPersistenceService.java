package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.ActiveSessionCache;
import java.util.UUID;

public interface SessionPersistenceService {
    // Этот метод - атомарная операция записи в Postgres
    AuthSession createAndSaveSession(Long userId, UUID sessionId, String refreshToken);

    // Этот метод - атомарная операция записи в Redis
    ActiveSessionCache createAndSaveRedisToken(AuthSession session);

    // Этот метод - атомарная операция записи в Аудит
    void createAndSaveAuditLog(AuthSession session);
}
