package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "revoked_sessions_archive", schema = "security") // Кладем в security-схему
@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString(of = {"sessionId", "reason"})
@NoArgsConstructor
@AllArgsConstructor
public class RevokedSessionArchive {

    @Id
    @Column(name = "session_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long userId;

    @Column(name = "fingerprint", columnDefinition = "TEXT")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String fingerprint;

    @Column(name = "ip_address", length = 45)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Instant expiresAt;

    @Column(name = "revoked_at", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private RevocationReason reason;
}