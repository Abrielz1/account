package ru.example.account.security.service.impl.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.service.strategy.RevocationExecutionChain;
import ru.example.account.security.service.strategy.StandardMassRevocationStrategy;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class StandardMassRevocationStrategyImpl implements StandardMassRevocationStrategy {

    private final RevocationExecutionChain revocationExecutionChain;

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public CompletableFuture<Boolean> execute(List<AuthSession> listActiveAuthSessions,
                                              SessionStatus status,
                                              RevocationReason reason) {

        Iterator<AuthSession> iterator = listActiveAuthSessions.iterator();

        try {

            while (iterator.hasNext()) {
                AuthSession iter = iterator.next();
                this.revocationExecutionChain.execute(iter, status, reason);
                iterator.remove();
            }
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }
        return CompletableFuture.completedFuture(listActiveAuthSessions.isEmpty());
    }
}
