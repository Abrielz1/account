package ru.example.account.security.service.strategy;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import java.util.concurrent.CompletableFuture;

public interface RevocationExecutionChain {

    /**
     * Executor conveyor, it's straight and linear
     * @param session
     * @param reason
     * @param status
     */
    boolean execute(AuthSession session, SessionStatus status, RevocationReason reason);

    CompletableFuture<Boolean> execute(Long userId, SessionStatus status, RevocationReason reason);
}
