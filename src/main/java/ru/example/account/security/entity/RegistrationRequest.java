package ru.example.account.security.entity;

import jakarta.persistence.Column;
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
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Where;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.proxy.HibernateProxy;
import ru.example.account.security.entity.enums.RegistrationStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Table(name = "registration_requests", schema = "security")
@Where(clause = "is_deleted = false")
@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {

    @Id@Column(name = "id", nullable = false, updatable = false)
    private UUID id;

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

    @Column(name = "email_hash", nullable = false, unique = true, updatable = false)
    private String emailHash;

    @Column(name = "phone_hash", nullable = false, unique = true, updatable = false)
    private String phoneHash;

    @Column(name = "username_hash", nullable = false, unique = true, updatable = false)
    private String usernameHash;

    @Column(name = "verification_token", nullable = false, unique = true, updatable = false)
    private String verificationToken;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "fingerprint_hash", nullable = false, updatable = false)
    private String fingerprintHash;

    @Column(name = "ip_address_hash", nullable = false)
    private String ipAddressHash;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "", nullable = false)
    private RegistrationStatus registrationStatus;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        RegistrationRequest that = (RegistrationRequest) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
