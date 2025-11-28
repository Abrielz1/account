package ru.example.account.security.service.impl.orcestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.facade.RedAlertRevocationStrategy;
import ru.example.account.security.service.orcestrator.SessionRevocationOrchestrator;
import ru.example.account.security.service.strategy.RevocationExecutionChain;
import ru.example.account.security.service.strategy.StandardMassRevocationStrategy;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import static ru.example.account.security.entity.SessionStatus.STATUS_ACTIVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionRevocationOrchestratorImpl implements SessionRevocationOrchestrator {

    private final RevocationExecutionChain revocationExecutionChain;

    private final RedAlertRevocationStrategy redAlertProtocolFacade;

    private final SessionQueryService queryService;

    private final StandardMassRevocationStrategy standardMassRevocationStrategy;

    @Override
    public boolean orchestrateSingleRevocation(AuthSession sessionToRevoke, SessionStatus status, RevocationReason reason) {

        if (sessionToRevoke == null) {
            log.warn("[WARN] session to revoke is empty!");
            return false;
        }

        if (!status.equals(STATUS_ACTIVE)) {
            log.warn("[WARN] session status MUST BE ACTIVE!");
            return false;
        }

        revocationExecutionChain.execute(sessionToRevoke, status, reason);

        return true;
    }

    @Async
    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<Boolean> orchestrateMassRevocation(Long userId, SessionStatus status, RevocationReason reason) {

        if (reason.equals(RevocationReason.REASON_RED_ALERT)) {
            log.info("[INFO] commence immediately proceed Red Alert Protocol!");
            return this.redAlertProtocolFacade.executeRedAlertProtocol(userId, status, reason);
        }

        List<AuthSession> listActiveAuthSessions = this.queryService.getAllActiveSession(userId, status);

        if (listActiveAuthSessions.isEmpty()) {
            log.warn("[WARN] no active sessions to revoke");
            return CompletableFuture.completedFuture(false);
        }

     return this.standardMassRevocationStrategy.execute(listActiveAuthSessions, status, reason);
    }
}
