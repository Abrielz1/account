package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "trusted_fingerprints", schema = "security")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustedFingerprint {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- СВЯЗЬ Many-to-One ("МНОГО Фингерпринтов -> ОДИН Профиль") ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_user_id", nullable = false)
    private UserFingerprintProfile profile;

    @Column(name = "fingerprint", nullable = false, columnDefinition = "TEXT")
    private String fingerprint;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "first_seen_at", nullable = false, updatable = false)
    private ZonedDateTime firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private ZonedDateTime lastSeenAt;

    @Column(name = "is_trusted", nullable = false)
    @Builder.Default
    private boolean isTrusted = true;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    // --- "Слоновьи", Hibernate-безопасные equals/hashCode, основанные на СУРРОГАТНОМ PK ---
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        TrustedFingerprint that = (TrustedFingerprint) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
