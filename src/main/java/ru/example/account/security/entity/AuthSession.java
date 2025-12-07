package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Where;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.proxy.HibernateProxy;
import ru.example.account.shared.util.AesCryptoConverter;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_sessions", schema = "security")
@Getter
@Builder
@Where(clause = "is_deleted = false")
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "userId", "status"})
public class AuthSession {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "refresh_token", columnDefinition = "TEXT", nullable = false, unique = true)
    private String refreshToken;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "access_token", columnDefinition = "TEXT", nullable = false, unique = true)
    private String accessToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SessionStatus status;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "fingerprint", columnDefinition = "TEXT")
    private String fingerprint; // "Сырой" фингерпринт для аналитики

    // ХЭШ ФИНГЕРПРИНТА ДЛЯ TOKEN BINDING
    @Column(name = "fingerprint_hash", columnDefinition = "TEXT", nullable = false)
    private String fingerprintHash;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revocation_reason")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private RevocationReason revocationReason;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Version
    private Long version;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        AuthSession that = (AuthSession) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    private void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    private void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    private void setRevocationReason(RevocationReason revocationReason) {
        this.revocationReason = revocationReason;
    }

    private void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    private void setStatus(SessionStatus status) {
        this.status = status;
    }

    public void revoke(RevocationReason reason, SessionStatus status) {

        if (this.status != SessionStatus.STATUS_ACTIVE) {
            throw new IllegalStateException("Cannot revoke an already inactive session: " + this.id);
        }

        this.setRevocationReason(reason);
        this.setRevokedAt(Instant.now())    ;
        this.setStatus(status);
        this.setExpiresAt(Instant.now());
        this.setRevocationReason(revocationReason);
        this.setDeleted(true);
    }
}