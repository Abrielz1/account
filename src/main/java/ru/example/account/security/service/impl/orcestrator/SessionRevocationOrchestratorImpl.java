package ru.example.account.security.service.impl.orcestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.service.orcestrator.SessionRevocationOrchestrator;
import ru.example.account.security.service.strategy.RevocationExecutionChain;
import java.util.concurrent.CompletableFuture;
import static ru.example.account.security.entity.SessionStatus.STATUS_ACTIVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionRevocationOrchestratorImpl implements SessionRevocationOrchestrator {

    private final RevocationExecutionChain revocationExecutionChain;

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

        return this.revocationExecutionChain.execute(sessionToRevoke, status, reason);
    }

    @Async
    @Override
    public CompletableFuture<Boolean> orchestrateMassRevocation(Long userId, SessionStatus status, RevocationReason reason) {

     return this.revocationExecutionChain.execute(userId, status, reason);
    }
}
