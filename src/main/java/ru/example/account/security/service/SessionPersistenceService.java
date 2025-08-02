package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.service.impl.AppUserDetails;
import java.time.ZonedDateTime;

public interface SessionPersistenceService {
    // Этот метод - атомарная операция запис
    AuthSession createAndSaveSession(AppUserDetails currentUser,
                                     String fingerprint,
                                     String ipAddress,
                                     String userAgent);

    // Этот метод - атомарная операция записи в Redis
    ActiveSessionCache createAndSaveActiveSessionCache(AuthSession session, AppUserDetails currentUser);

    // Этот метод - атомарная операция записи в Аудит
    void createAndSaveAuditLog(AuthSession session);

    void saveFingerPrint(String fingerprint, String ipAddress, String userAgent, ZonedDateTime lastSeenAt, Long userId);
}