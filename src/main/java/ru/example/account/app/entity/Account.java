package ru.example.account.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
/**
 * Банковский аккаунт пользователя.
 *
 * <p>Ограничения:
 * <ul>
 *   <li>balance >= 0</li>
 *   <li>initial_balance >= 0</li>
 *   <li>balance <= initial_balance * 2.07</li>
 * </ul>
 */
@Schema(description = "Bank account entity")
@Table(name = "accounts")
@Entity
@Getter
@Setter
@Builder
@ToString
@NamedEntityGraph(
        name = "account-with-user",
        attributeNodes = @NamedAttributeNode("user")
)
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {

    @Schema(description = "Unique account ID", example = "1")
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Current balance", example = "100.00")
    @EqualsAndHashCode.Include
    @Column(name = "balance", precision = 19, scale = 2)
    private BigDecimal balance;

    @Schema(description = "Initial deposit amount", example = "100.00")
    @EqualsAndHashCode.Include
    @Column(name = "initial_balance", precision = 19, scale = 2)
    private BigDecimal initialBalance;

    @OneToOne(mappedBy = "userAccount")
    @EqualsAndHashCode.Exclude()
    @ToString.Exclude
    @JsonIgnore
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
        Account account = (Account) o;
        return getId() != null && Objects.equals(getId(), account.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
