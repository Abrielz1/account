package ru.example.account.security.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.RevokedSessionArchive;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.service.AccessTokenBlacklistService;
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

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void revokeAndArchive(AuthSession sessionToRevoke, RevocationReason revocationReason) {

        if (sessionToRevoke == null) {
            log.error("Attempt to revoke a null session");
            return;
        }

        if (sessionToRevoke.getStatus() != SessionStatus.STATUS_ACTIVE) {
            log.error("Attempt to revoke an already inactive session with ID: {}. Revocation skipped.", sessionToRevoke.getId());
            return;
        }

        Instant now = Instant.now();

        RevokedSessionArchive newRevokedTokenArchive = RevokedSessionArchive.from(
                                                                            sessionToRevoke,
                                                                            now,
                                                                            revocationReason);


        revokedTokenArchiveRepository.save(newRevokedTokenArchive);

        authSessionRepository.delete(sessionToRevoke);

        try {
         Claims claims = jwtUtils.getAllClaimsFromToken(sessionToRevoke.getAccessToken());
         Instant expiration  = jwtUtils.getExpiration(claims);
            blacklistService.addToBlacklist(sessionToRevoke.getId(), Duration.between(now, expiration));
        } catch (Exception e) {
            log.warn("Could not parse access token for session {} to add to blacklist. It may be malformed or expired.", sessionToRevoke.getId());
        }

        log.info("Session {} for user {} has been REVOKED. Reason: {}", sessionToRevoke.getId(), sessionToRevoke.getUserId(), revocationReason);

        log.info("Session {} for user {} has been REVOKED with reason: {}",
                sessionToRevoke.getId(),
                sessionToRevoke.getUserId(),
                sessionToRevoke.getRevocationReason());
    }


    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void revokeAllSessionsForUser(Long userId, RevocationReason reason) {

        List<AuthSession> activeClientSessionsList = this.authSessionRepository.findAllByUserIdAndStatus(userId, reason)
                .stream()
                .map(s -> {
                    s.setStatus(SessionStatus.STATUS_COMPROMISED);
                    s.setRevokedAt(Instant.now());
                    s.setRevocationReason(reason);
                    return s;
                })
                .toList();

        authSessionRepository.saveAll(activeClientSessionsList);
        log.info("all clients sessions revoked successfully");
    }
}
