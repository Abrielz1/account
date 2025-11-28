package ru.example.account.security.service.worker;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;

/**
 *  Performs all cleanup operations in the "hot" in-memory storage (Redis).
 *  Its responsibilities include blacklisting tokens and deleting active
 *  session entries from the cache.
 */
public interface SessionCacheCleanupWorker {

    /**
     * Executes the full cleanup process in Redis for a given session.
     * This involves:
     *    Adding the access token to the Redis blacklist with a proper TTL
     *    Adding the refresh token to the Redis blacklist with a proper TTL.
     *    Deleting the active session object from the Redis cache.
     * @param sessionToRevoke The {@link AuthSession} being cleaned up.
     */
    void cleanup(AuthSession sessionToRevoke, RevocationReason revocationReason);
}
