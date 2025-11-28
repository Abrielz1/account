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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Where;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.proxy.HibernateProxy;
import ru.example.account.shared.util.AesCryptoConverter;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Entity
@Table(name = "revoked_sessions_archive", schema = "security") // Кладем в security-схему
@Getter
@Builder
@Where(clause = "is_deleted = false")
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

    // ХЭШ ФИНГЕРПРИНТА ДЛЯ TOKEN BINDING
    @Column(name = "fingerprint_hash", columnDefinition = "TEXT", nullable = false)
    private String fingerprintHash;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "fingerprint", columnDefinition = "TEXT")
    private String fingerprint;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Convert(converter = AesCryptoConverter.class)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SessionStatus sessionStatus;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    public static RevokedSessionArchive from(AuthSession sessionToRevoke, // todo под снос
                                             Instant now,
                                             RevocationReason revocationReason,
                                             SessionStatus sessionStatus) {

        if (sessionToRevoke == null) {
            // Чтобы избежать NullPointerException
            throw new IllegalArgumentException("Source AuthSession cannot be null");
        }

        return RevokedSessionArchive.builder().sessionId(sessionToRevoke.getId())
                .userId(sessionToRevoke.getUserId())
                .refreshToken(sessionToRevoke.getRefreshToken())
                .accessToken(sessionToRevoke.getAccessToken())
                .fingerprint(sessionToRevoke.getFingerprint())
                .fingerprintHash(sessionToRevoke.getFingerprintHash())
                .ipAddress(sessionToRevoke.getIpAddress())
                .userAgent(sessionToRevoke.getUserAgent())
                .createdAt(sessionToRevoke.getCreatedAt())
                .expiresAt(sessionToRevoke.getExpiresAt())
                .revokedAt(now)
                .reason(revocationReason)
                .sessionStatus(sessionStatus)
                .build();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        RevokedSessionArchive that = (RevokedSessionArchive) o;
        return getSessionId() != null && Objects.equals(getSessionId(), that.getSessionId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public void setUp(AuthSession currentSessionToRevoke, RevocationReason revocationReason, SessionStatus sessionStatus) {

        if (currentSessionToRevoke == null || !currentSessionToRevoke.getStatus().equals(SessionStatus.STATUS_REVOKED_BY_USER)) {
            log.warn("[WARN] session empty or status not STATUS_REVOKED_BY_USER!");
            throw new IllegalStateException("session empty or status not STATUS_REVOKED_BY_USER!");
        }

        this.sessionId = currentSessionToRevoke.getId();
        this.refreshToken = currentSessionToRevoke.getRefreshToken();
        this.accessToken = currentSessionToRevoke.getAccessToken();
        this.userId = currentSessionToRevoke.getUserId();
        this.fingerprintHash = currentSessionToRevoke.getFingerprintHash();
        this.fingerprint = currentSessionToRevoke.getFingerprint();
        this.ipAddress = currentSessionToRevoke.getIpAddress();
        this.userAgent = currentSessionToRevoke.getUserAgent();
        this.createdAt = currentSessionToRevoke.getCreatedAt();
        this.expiresAt = currentSessionToRevoke.getExpiresAt();
        this.revokedAt = currentSessionToRevoke.getRevokedAt();
        this.reason = revocationReason;
        this.sessionStatus = sessionStatus;
        this.isDeleted = true;
    }
}