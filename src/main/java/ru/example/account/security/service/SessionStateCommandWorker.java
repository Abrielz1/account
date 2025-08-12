package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;

/**
 * ВОРКЕР.
 */
public interface SessionStateCommandWorker {
    /**
     * Архивирует и "мягко удаляет" (изменяет статус) ОДНУ, сессию.
     * Должен, вызываться в своей собственной, новой транзакции.
     */
    void revokeAndArchiveSingleSession(
            AuthSession sessionToRevoke,
            SessionStatus newStatus,
            RevocationReason reason
    );
}
