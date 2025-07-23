package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import java.util.Optional;
import java.util.UUID;

public interface SessionQueryService {

    Optional<AuthSession> findById(UUID sessionId);

    Boolean checkExistenceOfFingerprint(String fingerprintHash);

    AuthSession findByRefreshToken(String refreshToken);

    Optional<AuthSession> findActiveByRefreshToken(String token);

    boolean isTokenArchived(String refreshToken);
}
