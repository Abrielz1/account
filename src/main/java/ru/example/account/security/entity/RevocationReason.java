package ru.example.account.security.entity;

public enum RevocationReason {
    /**
     * Отозван по причине штатного выхода пользователя.
     */
    REASON_USER_LOGOUT,

    /**
     * Отозван в результате штатной ротации refresh-токенов.
     */
    REASON_TOKEN_ROTATED,

    /**
     * Отозван принудительно администратором.
     */
    REASON_ADMIN_ACTION,

    /**
     * Отозван автоматически после смены пароля.
     */
    REASON_PASSWORD_CHANGE,
    /**
     * Критический статус. Означает, что система зафиксировала явную атаку
     * на сессию (например, попытка использовать refresh-token с нового устройства).
     * Этот статус должен запускать все необходимые алерты.
     */
    RED_ALERT
}
