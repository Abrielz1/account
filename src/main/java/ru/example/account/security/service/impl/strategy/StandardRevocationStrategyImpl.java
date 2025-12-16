package ru.example.account.security.service.impl.strategy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.enums.RevocationReason;
import ru.example.account.security.entity.enums.SessionStatus;
import ru.example.account.security.entity.WhiteListedAccessesToken;
import ru.example.account.security.entity.WhiteListedRefreshToken;
import ru.example.account.security.repository.WhiteListAccessTokenRepository;
import ru.example.account.security.repository.WhiteListRefreshTokenRepository;
import ru.example.account.security.service.strategy.RevocationExecutionChain;
import ru.example.account.security.service.worker.SessionArchiveWorker;
import ru.example.account.security.service.worker.SessionCacheCleanupWorker;
import ru.example.account.shared.exception.exceptions.ObjectNotFoundException;
import ru.example.account.shared.exception.exceptions.RevocationFailedException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardRevocationStrategyImpl implements RevocationExecutionChain {

    @PersistenceContext
    private final EntityManager entityManager;

    private final SessionArchiveWorker archiveWorker;

    private final SessionCacheCleanupWorker cleanupWorker;

    private final WhiteListAccessTokenRepository whiteListAccessTokenRepository;

    private final WhiteListRefreshTokenRepository whiteListRefreshTokenRepository;

    @Override
    @Retryable(
            retryFor = { OptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2, random = true)
    )
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public void execute(AuthSession currentSessionToRevoke, SessionStatus sessionStatus, RevocationReason revocationReason) {

        if (currentSessionToRevoke.getStatus() != SessionStatus.STATUS_ACTIVE) {
            log.warn("[WARN] Session {} is already inactive. Skipping.", currentSessionToRevoke.getId());
        }

        AuthSession backALiveSession = this.entityManager.merge(currentSessionToRevoke);

        WhiteListedAccessesToken whiteListedAccessesToken = whiteListAccessTokenRepository.findByToken(currentSessionToRevoke.getAccessToken())
                .orElseThrow(() -> {
                    log.warn("[WARN] session with access token {} is not fond!", currentSessionToRevoke.getAccessToken());
                    return new ObjectNotFoundException("session with access token is not fond!");
                });

        whiteListedAccessesToken.revoke(backALiveSession.getAccessToken(), revocationReason, sessionStatus);

        WhiteListedRefreshToken whiteListedRefreshToken = this.whiteListRefreshTokenRepository.findByToken(currentSessionToRevoke.getRefreshToken())
                .orElseThrow(() -> {
                    log.warn("[WARN] session with access token {} is not fond!", currentSessionToRevoke.getRefreshToken());
                    return new ObjectNotFoundException("session with refreshToken is not fond!");
                });

        whiteListedRefreshToken.revoke(backALiveSession.getRefreshToken(), revocationReason, sessionStatus);


        backALiveSession.revoke(revocationReason, sessionStatus);

        this.archiveWorker.archive(backALiveSession, revocationReason, sessionStatus);
        this.cleanupWorker.cleanup(backALiveSession, revocationReason);
    }

    @Recover
    public void recover(OptimisticLockingFailureException e, AuthSession session) {

        log.error("FATAL: Could not revoke session {} after all retries due to persistent optimistic lock conflict.",
                session.getId(), e);
        throw new RevocationFailedException("Unrecoverable conflict for session "
                + session.getId() + System.lineSeparator()
                + e.getMessage());
    }
}
