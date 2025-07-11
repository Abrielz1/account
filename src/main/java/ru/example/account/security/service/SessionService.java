package ru.example.account.security.service;


import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import java.util.Optional;
import java.util.UUID;

public interface SessionService {

    AuthSession createNew(Long userId, String fingerprint, String ip, String userAgent);

    Optional<AuthSession> findActiveByRefreshToken(String token);

    Optional<AuthSession> findById(UUID sessionId);

    void archive(AuthSession session, RevocationReason reason);

    void archiveAllForUser(Long userId, RevocationReason reason);
}
