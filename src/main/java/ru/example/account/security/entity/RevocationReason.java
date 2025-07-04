package ru.example.account.security.entity;

public enum RevocationReason {
    USER_LOGOUT,
    TOKEN_ROTATED,
    ADMIN_ACTION,
    PASSWORD_CHANGE,
    /**
     * Критический статус. Означает, что система зафиксировала явную атаку
     * на сессию (например, попытка использовать refresh-token с нового устройства).
     * Этот статус должен запускать все необходимые алерты.
     */
    RED_ALERT
}
