package ru.example.account.security.service.orcestrator;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.enums.RevocationReason;
import ru.example.account.security.entity.enums.SessionStatus;

import java.util.concurrent.CompletableFuture;

/**
 * ORCHESTRATOR. The brain of the session lifecycle management process.
 * Contains the complex, multi-step business logic and coordinates calls to
 * various low-level, specialized workers. Its contract mirrors the Facade's for clean delegation.
 */
public interface SessionRevocationOrchestrator {

    /**
     * Orchestrates the full, multi-worker process for revoking a single session.
     * This includes calling persistence workers, cache workers, and blacklist workers in the correct order.
     * @return boolean - true if all orchestrated workers (DB, Cache, etc.) completed
     */
    boolean orchestrateSingleRevocation(AuthSession sessionToRevoke, SessionStatus status, RevocationReason reason);

    /**
     * Orchestrates the complex process of finding all active sessions for a user
     * and then revoking each one individually, while potentially triggering
     * blocking and notification workers.
     *
     * @return boolean - true if the mass revocation process (finding sessions + calling workers) was successful.
     */
    CompletableFuture<Boolean> orchestrateMassRevocation(Long userId, SessionStatus status, RevocationReason reason);
}