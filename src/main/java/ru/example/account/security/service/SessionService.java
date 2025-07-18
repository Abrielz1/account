package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.service.impl.AppUserDetails;
import java.util.Optional;
import java.util.UUID;

public interface SessionService {

    Optional<AuthSession> findActiveByRefreshToken(String token);

    Optional<AuthSession> findById(UUID sessionId);

    void archive(AuthSession session, RevocationReason reason);

    void archiveAllForUser(Long userId, RevocationReason reason);

    void createNewSession(AppUserDetails userDetails,
                                 String fingerprint,
                                 String ipAddress,
                                 String userAgent,
                                 String accessesToken,
                                 String refreshToken);
}
