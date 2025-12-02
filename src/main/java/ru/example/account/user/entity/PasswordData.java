package ru.example.account.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import ru.example.account.shared.util.AesCryptoConverter;
import java.time.Instant;
import java.util.Objects;

@NamedEntityGraph(name = "PasswordData.withUser", attributeNodes = @NamedAttributeNode("user"))
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@Entity
@Table(name = "password_data")
@AllArgsConstructor
public class PasswordData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "tine_when_set", nullable = false)
    private Instant timeWhenSet;

    @Column(name = "time_to_live", nullable = false)
    private Instant timeToLive;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "_fingerprint_hash", nullable = false)
    private String fingerPrintHash;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "ip_adres", nullable = false)
    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Version
    private Long version;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        PasswordData emailData = (PasswordData) o;
        return getId() != null && Objects.equals(getId(), emailData.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
