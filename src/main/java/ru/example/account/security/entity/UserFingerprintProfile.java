package ru.example.account.security.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.proxy.HibernateProxy;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "user_fingerprint_profiles", schema = "security")
@Getter
@Setter
@Builder
@Where(clause = "is_deleted = false")
@NoArgsConstructor
@AllArgsConstructor
public class UserFingerprintProfile {
    @Id
    @Column(name = "user_id")
    private Long userId;

    // СВЯЗЬ: ОДИН профиль ВЛАДЕЕТ МНОЖЕСТВОМ "отпечатков"
    @OneToMany(
            mappedBy = "profile", // "Управляется" полем "profile" в классе TrustedFingerprint
            cascade = CascadeType.ALL,    // Если сохраняем/удаляем Профиль, то же самое делаем и с его Фингерпринтами
            orphanRemoval = true,       // Если удаляем Фингерпринт из этого Set-а, он удаляется и из БД
            fetch = FetchType.LAZY        // Загружаем фингерпринты только по требованию
    )
    @Builder.Default
    private Set<TrustedFingerprint> trustedFingerprints = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private ZonedDateTime lastUpdatedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserFingerprintProfile that = (UserFingerprintProfile) o;
        // Используем userId, ТАК КАК ЭТО PK, но с защитой от null!
        return getUserId() != null && Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    public void addFingerprint(TrustedFingerprint fingerprint) {
        this.trustedFingerprints.add(fingerprint);
        fingerprint.setProfile(this);
    }

    public void removeFingerprint(TrustedFingerprint fingerprint) {
        this.trustedFingerprints.remove(fingerprint);
        fingerprint.setProfile(null);
    }
}
