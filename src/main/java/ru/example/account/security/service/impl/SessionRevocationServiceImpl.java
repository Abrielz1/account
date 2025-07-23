package ru.example.account.security.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.RevokedSessionArchive;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.ActiveSessionCacheRepository;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.repository.SessionAuditLogRepository;
import ru.example.account.security.service.AccessTokenBlacklistService;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.SessionRevocationService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionRevocationServiceImpl implements SessionRevocationService {

    private final AuthSessionRepository authSessionRepository;

    private final AccessTokenBlacklistService blacklistService;

    private final JwtUtils jwtUtils;

    private final RevokedTokenArchiveRepository revokedTokenArchiveRepository;

    private final SessionQueryService sessionQueryService;

    private final SessionAuditLogRepository auditLogRepository;

    private final ActiveSessionCacheRepository activeSessionCacheRepository;

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void revokeAndArchive(AuthSession sessionToRevoke, RevocationReason revocationReason) {

        if (sessionToRevoke == null) {
            log.warn("Attempt to revoke a null session");
            return;
        }

        if (sessionToRevoke.getStatus() != SessionStatus.STATUS_ACTIVE) {
            log.warn("Attempt to revoke an already inactive session with ID: {}. Revocation skipped.", sessionToRevoke.getId());
            return;
        }

        Instant now = Instant.now();

        this.auditLogRepository.findBySessionId(sessionToRevoke.getId()).ifPresent(auditLog -> {
            // Вся логика внутри лямбды. Hibernate видит изменения.
            if (revocationReason == RevocationReason.REASON_RED_ALERT ||
                    revocationReason == RevocationReason.REASON_REVOKED_BY_USER_ON_ALL_DEVICES_SECURITY_ATTENTION) {

                auditLog.setCompromised (true);
                log.debug("Marking session {} as COMPROMISED in audit log.", sessionToRevoke.getId());
            }
        });

        log.debug("Revocation for session {} recorded in audit log.", sessionToRevoke.getId());

        RevokedSessionArchive newRevokedTokenArchive = RevokedSessionArchive.from(
                                                                            sessionToRevoke,
                                                                            now,
                                                                            revocationReason);
        // ШАГ 1: АРХИВИРУЕМ (POSTGRES)
        this.revokedTokenArchiveRepository.save(newRevokedTokenArchive);

        // ШАГ 2: БЛЭКЛИСТИМ (REDIS)
        try {
         Claims claims = jwtUtils.getAllClaimsFromToken(sessionToRevoke.getAccessToken());
         this.blacklistService.addToBlacklist(sessionToRevoke.getId(), Duration.between(now, jwtUtils.getExpiration(claims)));
        } catch (Exception e) {
            log.warn("Could not parse access token for session {} to add to blacklist. It may be malformed or expired.", sessionToRevoke.getId());
        }

        // ШАГ 3: ЧИСТИМ "ГОРЯЧИЕ" ХРАНИЛИЩА (POSTGRES + REDIS)
        activeSessionCacheRepository.deleteById(sessionToRevoke.getRefreshToken());
        authSessionRepository.delete(sessionToRevoke);

        log.info("Session {} for user {} has been REVOKED. Reason: {}", sessionToRevoke.getId(), sessionToRevoke.getUserId(), revocationReason);

        log.info("Session {} for user {} has been REVOKED with reason: {}",
                sessionToRevoke.getId(),
                sessionToRevoke.getUserId(),
                sessionToRevoke.getRevocationReason());
    }


    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void revokeAllSessionsForUser(Long userId, SessionStatus status, RevocationReason reason) {

        List<AuthSession> activeClientSessionsList = this.sessionQueryService.getAllActiveSession(userId, SessionStatus.STATUS_ACTIVE);

        if (activeClientSessionsList.isEmpty()) {
            log.info("No active sessions!");
            return;
        }

        activeClientSessionsList.parallelStream().forEach(authSession -> {
            try {
                this.revokeAndArchive(authSession, reason);
            } catch (DataAccessException dataAccessException) {
                log.error("Failed to revoke session {}. Continuing...", authSession.getId(), dataAccessException);
            } catch (RuntimeException runtimeException) {
                log.error("Something goes wrong: {}", runtimeException.getMessage());
            }
        });

        log.info("all clients sessions revoked successfully");
    }
}
