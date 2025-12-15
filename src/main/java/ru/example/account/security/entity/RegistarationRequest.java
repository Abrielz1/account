package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Where;

import java.time.Instant;

@Entity
@Table(name = "auth_sessions", schema = "security")
@Getter
@Builder
@Where(clause = "is_deleted = false")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegistarationRequest {

    @Id
    @Column(name = "id", unique = true, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_email_sent", nullable = false)
    private Boolean isEmailSent = false;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "is_security_approved", nullable = false)
    private Boolean isSecurityApproved = false;

    @Column(name = "is_finalized", nullable = false)
    private Boolean isFinalized = false;

    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired = false;

    @Column(name = "is_rejected", nullable = false)
    private Boolean isRejected = false;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "password_hash", nullable = false, updatable = false)
    private String passwordHash;

    @Column(name = "email_hash", nullable = false, updatable = false)
    private String emailHash;

    @Column(name = "phone_hash", nullable = false, updatable = false)
    private String phoneHash;

    @Column(name = "username_hash", nullable = false, updatable = false)
    private String usernameHash;

    @Column(name = "verification_token", nullable = false, unique = true, updatable = false)
    private String verificationToken;

    @Column(name = "expires_at", nullable = false, unique = true, updatable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, unique = true, updatable = false)
    private Instant createdAt;

    @Column(name = "fingerprint_hash", nullable = false, unique = true, updatable = false)
    private String fingerprintHash;

    @Column(name = "fingerprint", nullable = false, unique = true, updatable = false)
    private String fingerprint;

    @Column(name = "ip_address_hash", nullable = false, unique = true)
    private String ipAddressHash;

    @Column(name = "ip_address", nullable = false, unique = true)
    private String ipAddress;
}
