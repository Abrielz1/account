package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_fingerprints", schema = "security")
@Getter
@Setter
public class SessionFingerprint {

    @Id
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fingerprint_hash", nullable = false)
    private String fingerprintHash;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "is_disabled", nullable = false)
    private boolean isDisabled;

    @Column(name = "is_blacklisted", nullable = false)
    private boolean isBlacklisted;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
}
