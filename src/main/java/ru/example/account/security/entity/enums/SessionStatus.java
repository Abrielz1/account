package ru.example.account.security.entity.enums;

public enum SessionStatus {
    /**
     * Сессия активна и валидна.
     */
    STATUS_ACTIVE,

    /**
     * Сессия скомпрометирована
     */
    STATUS_COMPROMISED,

    /**
     * Сессия потенциально скомпрометирована СБ проверить потенциальную угрозу.
     */
    STATUS_POTENTIAL_COMPROMISED,

    /**
     * Сессия отозвана пользователем (logout).
     */
    STATUS_REVOKED_BY_USER,


    /**
     * Сессия принудительно отозвана системой/администратором.
     */
    STATUS_REVOKED_BY_SYSTEM,

    /**
     * КРАСНАЯ ТРЕВОГА: Сессия распознана как хакерская атака.
     */
    STATUS_RED_ALERT
}
