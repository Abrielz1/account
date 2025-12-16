package ru.example.account.security.entity.enums;

public enum RegistrationStatus {
    PENDING_EMAIL_VERIFICATION,
    PENDING_SECURITY_APPROVAL,
    APPROVED, REJECTED, FINALIZED,
    EXPIRE
}
