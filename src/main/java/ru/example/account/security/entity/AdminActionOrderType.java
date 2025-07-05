package ru.example.account.security.entity;

public enum AdminActionOrderType {
    FREEZE_ACCOUNT,
    UNFREEZE_ACCOUNT,
    CLOSE_ACCOUNT,
    BAN_USER,
    UNBAN_USER,
    REVOKE_ALL_SESSIONS;
}
