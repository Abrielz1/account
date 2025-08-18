package ru.example.account.security.service.facade;

import ru.example.account.security.entity.AuthSession;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.UUID;

/**
 * High-level FACADE for all interactions with the security cache layer (Redis).
 * It aggregates multiple low-level cache/blacklist workers and provides a simple,
 * clean, boolean-based API for higher-level Orchestrators.
 * Its primary responsibility is to hide the implementation details of cache validation and manipulation.
 */
public interface SecurityCacheFacade {

    /**
     * Finds and meticulously verifies a session using the full cache and database validation logic.
     * This is the single, authoritative entry point for checking session validity.
     * The implementation of this facade is responsible for handling various failure modes
     * (e.g., IMPOSTER vs STALE vs NOT_FOUND) and escalating security events if necessary.
     *
     * @param sessionId              The UUID of the session to be verified.
     * @param currentUserId          The User ID extracted from the current JWT.
     * @param currentUserAuthorities The authorities extracted from the current JWT.
     * @return boolean - Returns true ONLY if the session is fully validated and considered secure.
     *                   Returns false in all other scenarios.
     */
    boolean isSessionValid(
            UUID sessionId,
            Long currentUserId,
            Collection<? extends GrantedAuthority> currentUserAuthorities
    );

    /**
     * Completely purges a session and its associated tokens from the entire cache system.
     * The implementation delegates to both the ActiveSessionCacheCommandWorker (for deletion)
     * and the BlacklistCommandWorker (for blacklisting).
     * This is intended as a "fire-and-forget" command for the Orchestrator layer.
     *
     * @param session The session to be purged and blacklisted.
     * @return boolean - Returns true if the purge and blacklist commands were executed
     *                   without any critical, unhandled exceptions.
     */
    boolean purgeAndBlacklistSession(AuthSession session);
}