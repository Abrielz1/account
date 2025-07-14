package ru.example.account.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.proxy.HibernateProxy;
import ru.example.account.security.model.request.UserRegisterRequestDto;
import java.util.Objects;

@Entity
@Table(name = "customers", schema = "business")
@DiscriminatorValue("CUSTOMER")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Client extends User {

    @Enumerated(EnumType.STRING)
    @Column(name = "loyalty_status")
    @JdbcType(PostgreSQLEnumJdbcType.class) // Для кастомного ENUM в PG
    @ToString.Include
    private LoyaltyStatus loyaltyStatus;

    @Column(name = "registration_source")
    @ToString.Include
    private String registrationSource;

    @Column(name = "is_banned", nullable = false)
    @ToString.Include
    private Boolean isBanned = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    @ToString.Exclude
    private Client invitedBy;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;

        Client customer = (Client) o;

        return getId() != null && Objects.equals(getId(), customer.getId());
    }

    @Override
    public final int hashCode() {
        // Мы используем хэш-код "настоящего" класса, чтобы избежать проблем с прокси.
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    public void setFieldsClient(UserRegisterRequestDto request) {

        this.setDateOfBirth(request.birthDate());
        this.getRoles().add(RoleType.ROLE_CLIENT);
        this.setLoyaltyStatus(LoyaltyStatus.BRONZE);
        this.setIsBanned(false);
        this.setUsername(request.username());
    }
}
