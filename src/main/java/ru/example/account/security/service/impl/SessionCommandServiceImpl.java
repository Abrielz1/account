package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.RevokedClientData;
import ru.example.account.security.entity.RevokedSessionArchive;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedDataRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.service.IdGenerationService;
import ru.example.account.security.service.SessionCommandService;
import ru.example.account.security.service.SessionRevocationService;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCommandServiceImpl implements SessionCommandService {

    private final RevokedDataRepository revokedDataRepository;

    private final IdGenerationService idGenerationService;

    private final SessionRevocationService sessionRevocationService;

    private final AuthSessionRepository authSessionRepository;

    private final RevokedTokenArchiveRepository revokedTokenArchiveRepository;

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void archive(AuthSession sessionToRevoke, RevocationReason reason) {

        Instant now = Instant.now();

        // 2. Помечаем основную сессию как отозванную. Мы ее НЕ удаляем.
        sessionToRevoke.setStatus(sessionToRevoke.getStatus() == SessionStatus.STATUS_COMPROMISED ?
                SessionStatus.STATUS_RED_ALERT : SessionStatus.STATUS_REVOKED_BY_USER);

        if (sessionToRevoke.getStatus().equals(SessionStatus.STATUS_RED_ALERT)) {

            sessionRevocationService.revokeAllSessionsForUser(sessionToRevoke.getUserId(), RevocationReason.REASON_RED_ALERT);
            log.trace("");
            throw new SecurityBreachAttemptException("");
        }

        if (sessionToRevoke.getStatus() != SessionStatus.STATUS_ACTIVE) {
            log.error("Attempt to revoke an already inactive session with ID: {}. Revocation skipped.", sessionToRevoke.getId());
            return;
        }

        RevokedSessionArchive newRevokedTokenArchive = RevokedSessionArchive.builder()
                .sessionId(sessionToRevoke.getId())
                .userId(sessionToRevoke.getUserId())
                .fingerprint(sessionToRevoke.getFingerprint())
                .ipAddress(sessionToRevoke.getIpAddress())
                .userAgent(sessionToRevoke.getUserAgent())
                .createdAt(sessionToRevoke.getCreatedAt())    // Время создания оригинальной сессии
                .expiresAt(sessionToRevoke.getExpiresAt())      // Когда она должна была истечь
                .revokedAt(now)                           // Время фактического отзыва
                .reason(reason)
                .build();

        revokedTokenArchiveRepository.save(newRevokedTokenArchive);
        log.info("Session archived successfully!");

        authSessionRepository.save(sessionToRevoke);
        log.info("session successfully saved");
        this.sessionRevocationService.revoke(newRevokedTokenArchive);
    }

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void archiveAllForUser(Long userId, String fingerprint, String ipAddress, String userAgent, RevocationReason revocationReason) {

        RevokedClientData data = RevokedClientData.builder()
                .id(this.idGenerationService.generateSessionId()) // Уникальный ID инцидента
                .userId(userId)
                .fingerprint(fingerprint)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAlertAt(Instant.now())
                .revocationReason(revocationReason)
                .build();
        revokedDataRepository.save(data);

        sessionRevocationService.revokeAllSessionsForUser(userId, RevocationReason.REASON_RED_ALERT);
    }
}
