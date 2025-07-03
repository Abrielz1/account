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

    private String fingerprintHash;
    private String ipAddress;
    private String userAgent;

    @Column(nullable = false)
    private Instant createdAt;
}
