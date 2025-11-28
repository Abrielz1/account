package ru.example.account.security.service.strategy;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StandardMassRevocationStrategy {

     /**
     *
     * @param listActiveAuthSessions
     * @param reason
     * @param status
     */
     public CompletableFuture<Boolean> execute(List<AuthSession> listActiveAuthSessions,
                                               SessionStatus status,
                                               RevocationReason reason);
}
