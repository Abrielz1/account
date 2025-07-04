package ru.example.account.security.entity;

public enum SessionStatus {
    ACTIVE,
    REVOKED_BY_USER,
    REVOKED_BY_SYSTEM,
    RED_ALERT
}
