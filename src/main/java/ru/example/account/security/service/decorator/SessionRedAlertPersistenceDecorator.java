package ru.example.account.security.service.decorator;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.enums.RevocationReason;
import ru.example.account.security.entity.enums.SessionStatus;
import java.util.List;

/**
 * DECORATOR.
 * This decorator knows how to handle a BATCH, of sessions,
 * unlike the "simple worker". It DECORATES the single-session logic with batch-processing capabilities.
 */
public interface SessionRedAlertPersistenceDecorator {
    /**
     * Takes a LIST, of sessions, iterates through them, and calls
     * the simple worker (TransactionalSingleSessionWorker) for EACH one.
     * This ensures each revocation is its own atomic transaction.
     *
     * @param sessionsToRevoke The BATCH, of sessions to be executed.
     * @return true if the overall batch operation was considered successful.
     */
    boolean revokeAllSessionsForUser(List<AuthSession> sessionsToRevoke, SessionStatus status, RevocationReason reason);
}
