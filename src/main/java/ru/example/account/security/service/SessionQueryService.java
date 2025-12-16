package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.enums.SessionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionQueryService {

    Optional<AuthSession> findById(UUID sessionId);

    Boolean checkExistenceOfFingerprint(String fingerprintHash);

    Optional<AuthSession> findByRefreshTokenAndStatus(String refreshToken);

    Optional<AuthSession> findActiveByAccessToken(String token);

    boolean isTokenArchived(String refreshToken);

    List<AuthSession> getAllActiveSession(Long userId, SessionStatus sessionStatus);

    Optional<String> getFingerPrint(String refreshToken);
}
