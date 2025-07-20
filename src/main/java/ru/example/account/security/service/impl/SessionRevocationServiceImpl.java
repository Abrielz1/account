package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.RevokedTokenArchive;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.service.AccessTokenBlacklistService;
import ru.example.account.security.service.SessionRevocationService;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionRevocationServiceImpl implements SessionRevocationService {

    private final AuthSessionRepository authSessionRepository;

    private final RevokedTokenArchiveRepository archiveRepository;

    private final AccessTokenBlacklistService blacklistService;

    @Override
    public void revoke(AuthSession sessionToRevoke, RevocationReason reason) {
        Instant now = Instant.now();

        if (sessionToRevoke == null) {
            log.error("Attempt to revoke a null session");
            return;
        }

        if (sessionToRevoke.getStatus() != SessionStatus.STATUS_ACTIVE) {
            log.error("Attempt to revoke an already inactive session with ID: {}", sessionToRevoke.getId());
            return;
        }

        RevokedTokenArchive newRevokedTokenArchive = RevokedTokenArchive.builder()
                .refreshTokenValue(sessionToRevoke.getRefreshToken())
                .accessesTokenValue(sessionToRevoke.getAccessToken())
                .sessionId(sessionToRevoke.getId())
                .reason(reason)
                .revokedAt(now)
                .userId(sessionToRevoke.getUserId())
                .fingerprint(sessionToRevoke.getFingerprint())
                .build();

        // 2. Помечаем основную сессию как отозванную. Мы ее НЕ удаляем.
        sessionToRevoke.setStatus(sessionToRevoke.getStatus() == SessionStatus.STATUS_COMPROMISED ?
                SessionStatus.STATUS_RED_ALERT : SessionStatus.STATUS_REVOKED_BY_SYSTEM);
        sessionToRevoke.setRevokedAt(now);
        sessionToRevoke.setReason(reason);
        newRevokedTokenArchive.setSessionStatus(sessionToRevoke.getStatus());
        newRevokedTokenArchive.setReason(reason);

        log.info("Session {} for user {} has been REVOKED. Reason: {}", sessionToRevoke.getId(), sessionToRevoke.getUserId(), reason);
        archiveRepository.save(newRevokedTokenArchive);
        log.info("Session archived successfully!");
        authSessionRepository.save(sessionToRevoke);
        log.info("session successfully saved");

    }

    @Override
    public void revokeAllSessionsForUser(Long userId, RevocationReason reason) {

    }
}
