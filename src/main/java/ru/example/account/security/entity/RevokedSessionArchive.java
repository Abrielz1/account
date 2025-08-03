package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import ru.example.account.shared.util.AesCryptoConverter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "revoked_sessions_archive", schema = "security") // Кладем в security-схему
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "sessionId")
@ToString()
@NoArgsConstructor
@AllArgsConstructor
public class RevokedSessionArchive {

    @Id
    @Column(name = "session_id")
    private UUID sessionId;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "refresh_token", unique = true)
    private String refreshToken;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "access_token", unique = true)
    private String accessToken;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "fingerprint", columnDefinition = "TEXT")
    private String fingerprint;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private RevocationReason reason;

    public static RevokedSessionArchive from(AuthSession sessionToRevoke,
                                             Instant now,
                                             RevocationReason revocationReason) {

        if (sessionToRevoke == null) {
            // Чтобы избежать NullPointerException
            throw new IllegalArgumentException("Source AuthSession cannot be null");
        }

        return RevokedSessionArchive.builder().sessionId(sessionToRevoke.getId())
                .userId(sessionToRevoke.getUserId())
                .refreshToken(sessionToRevoke.getRefreshToken())
                .accessToken(sessionToRevoke.getAccessToken())
                .fingerprint(sessionToRevoke.getFingerprint())
                .ipAddress(sessionToRevoke.getIpAddress())
                .userAgent(sessionToRevoke.getUserAgent())
                .createdAt(sessionToRevoke.getCreatedAt())
                .expiresAt(sessionToRevoke.getExpiresAt())
                .revokedAt(now)
                .reason(revocationReason)
                .build();
    }
}