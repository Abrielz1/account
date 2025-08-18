package ru.example.account.security.service.worker;


import ru.example.account.security.entity.AuthSession;

/**
 * WORKER for all cache and blacklist operations in Redis.
 * Its ONLY job is to write to the fast, in-memory store.
 */
public interface SessionCacheAndBlacklistWorker {

    /**
     * Adds the tokens from a given session to the hot-cache blacklist.
     */
    void blacklistTokens(AuthSession session);

    /**
     * Deletes a session's entry from the active session cache.
     */
    void deleteFromActiveSessionCache(AuthSession session);
}
