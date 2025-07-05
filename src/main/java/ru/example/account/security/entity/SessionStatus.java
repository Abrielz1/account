package ru.example.account.security.entity;

public enum SessionStatus {
    /**
     * Сессия активна и валидна.
     */
    STATUS_ACTIVE,

    /**
     * Сессия отозвана пользователем (logout).
     */
    STATUS_REVOKED_BY_USER,

    /**
     * Сессия принудительно отозвана системой/администратором.
     */
    STATUS_REVOKED_BY_SYSTEM,

    /**
     * КРАСНАЯ ТРЕВОГА: Сессия помечена как скомпрометированная.
     */
    STATUS_RED_ALERT
}
