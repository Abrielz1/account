package ru.example.account.security.service.facade;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;

/**
 * High-level FACADE for all session revocation scenarios.
 * This is the single, clean entry point for external callers (Controllers, Filters, etc.).
 * Its sole responsibility is to hide the internal system complexity by delegating
 * all calls to a dedicated orchestration layer. It contains NO business logic.
 */
public interface SessionRevocationService {

   /**
    * Initiates the standard, graceful revocation of a SINGLE session.
    * This is the primary method for routine operations like user logout or
    * regular token rotation where a specific session is targeted.
    *
    * @param sessionToRevoke The complete AuthSession entity that needs to be revoked.
    * @param status          The new status to be assigned to the session (e.g., STATUS_REVOKED_BY_USER).
    * @param reason          The explicit business reason for the revocation (e.g., REASON_USER_LOGOUT).
    * @return boolean - **true if the revocation was successful**, false otherwise.
    */
   boolean revokeAndArchive(AuthSession sessionToRevoke, SessionStatus status, RevocationReason reason);

   /**
    * Initiates the emergency, system-wide revocation of ALL active sessions for a specific user.
    * This is the "Red Button" for critical security events like a suspected compromise.
    *
    * @param userId The ID of the user whose sessions will be globally revoked.
    * @param status The new, typically severe, status to be assigned to all sessions (e.g., STATUS_COMPROMISED).
    * @param reason The explicit reason for this mass revocation (e.g., REASON_RED_ALERT).
    * @return boolean - **true if the entire mass revocation process was successful.
    */
   boolean revokeAllSessionsForUser(Long userId, SessionStatus status, RevocationReason reason);
}