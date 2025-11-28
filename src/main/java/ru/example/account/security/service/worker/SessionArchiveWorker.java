package ru.example.account.security.service.worker;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;

/**
 *  Performs all archival operations. Its sole responsibility is to write
 *  revocation records to the "cold", long-term persistence storage (Postgres).
 * ======================================================================
 */
public interface SessionArchiveWorker {

    /**
     * Creates all necessary audit and archival records for a revoked session.
     * This includes creating entries in the `RevokedSessionArchive`,
     * `BlacklistedAccessToken`, and `BlacklistedRefreshToken` tables.
     *
     * @param sessionToRevoke   The {@link AuthSession} being revoked.
     */
    void archive(AuthSession sessionToRevoke, RevocationReason revocationReason, SessionStatus sessionStatus);
}
