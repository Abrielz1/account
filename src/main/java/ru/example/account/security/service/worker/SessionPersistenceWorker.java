package ru.example.account.security.service.worker;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;

/**
 * WORKER for all PERSISTENT session state changes.
 * Its ONLY job is to interact with the database tables. It is blind to the existence of caches.
 */
public interface SessionPersistenceWorker {

    /**
     * Moves a session to the archive and nd "soft-deletes" in Postgres.
     * Must be in its own, new transaction for atomicity during mass revocations.
     * @return boolean - true if the database operations were successful.
     */
    boolean archiveAndMarkAsRevoked(AuthSession session, SessionStatus status, RevocationReason reason);
}