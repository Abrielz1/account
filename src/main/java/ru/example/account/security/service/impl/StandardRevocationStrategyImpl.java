package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.RevokedClientData;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedDataRepository;
import ru.example.account.security.service.worker.ActiveSessionCacheCommandWorker;
import ru.example.account.security.service.worker.BlacklistCommandWorker;
import ru.example.account.security.service.worker.IdGenerationService;
import ru.example.account.security.service.RevocationStrategy;
import ru.example.account.shared.exception.exceptions.SessionNotFoundException;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardRevocationStrategyImpl implements RevocationStrategy {

    private final RevokedDataRepository revokedDataRepository;

    private final IdGenerationService idGenerationService;

    private final BlacklistCommandWorker blacklistCommandWorker;

    private final AuthSessionRepository authSessionRepository;

    private final ActiveSessionCacheCommandWorker activeSessionCacheCommandWorker;

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public boolean archiveAllForUser(Long userId,
                                  String fingerprint,
                                  String ipAddress,
                                  String userAgent,
                                  RevocationReason revocationReason) {

        AuthSession currentSession = this.authSessionRepository.findByUserIdAndFAndFingerprint(userId, fingerprint).orElseThrow(() -> {
            log.info("[WARN] session not fond!");
            return new SessionNotFoundException("session not fond!");
        });

        RevokedClientData data = RevokedClientData.builder()
                .id(this.idGenerationService.generateSessionId())
                .userId(userId)
                .fingerprint(fingerprint)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAlertAt(Instant.now())
                .revocationReason(revocationReason)
                .build();

        this.revokedDataRepository.save(data);

        this.activeSessionCacheCommandWorker.deleteSessionById(currentSession.getId());
        this.blacklistCommandWorker.blacklistAccessToken(currentSession.getAccessToken());
        this.blacklistCommandWorker.blacklistRefreshToken(currentSession.getRefreshToken());

        currentSession.revoke(revocationReason, currentSession.getStatus());

        this.authSessionRepository.save(currentSession);

     return true;
    }
}
