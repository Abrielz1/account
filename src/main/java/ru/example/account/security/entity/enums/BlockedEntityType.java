package ru.example.account.security.entity.enums;

public enum BlockedEntityType {
    IP_ADDRESS,
    USER_ID,
    FINGERPRINT,
    USER_AGENT_PATTERN // банить по маске User-Agent'a
}
