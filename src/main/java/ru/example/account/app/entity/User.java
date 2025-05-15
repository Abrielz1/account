package ru.example.account.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
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

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
/**
 * Сущность пользователя системы.
 * Содержит учетные данные, роли и связанные данные (телефоны, emails, аккаунт).
 *
 * <p>Атрибуты:
 * <ul>
 *   <li>id - Уникальный идентификатор пользователя</li>
 *   <li>username - Логин (3-255 символов, уникальный)</li>
 *   <li>password - Хэш пароля (BCrypt)</li>
 *   <li>roles - Набор ролей пользователя</li>
 *   <li>userPhones - Список привязанных телефонов</li>
 *   <li>userEmails - Список привязанных email-адресов</li>
 *   <li>userAccount - Связанный банковский аккаунт</li>
 *   <li>version - Оптимистичная блокировка</li>
 * </ul>
 */
@Table(name = "users")
@Entity
@Getter
@Setter
@Builder
@ToString
@NamedEntityGraph(
        name = "user-with-contacts",
        attributeNodes = {
                @NamedAttributeNode("roles"),
                @NamedAttributeNode("userPhones"),
                @NamedAttributeNode("userEmails")
        }
)
@NamedEntityGraph(
        name = "user-with-accounts",
        attributeNodes = @NamedAttributeNode("userAccount")
)
@NamedEntityGraph(name = "users_entity-graph", attributeNodes = @NamedAttributeNode("roles"))
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(nullable = false, name = "username", unique = true, length = 255)
    private String username;

    @Column(nullable = false, name = "password", length = 3200)
    private String password;

    @EqualsAndHashCode.Include
    @Column(nullable = false, name = "date_of_birth")
    private LocalDate dateOfBirth;

    @ElementCollection(targetClass = RoleType.class, fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Set<RoleType> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    Set<PhoneData> userPhones = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    Set<EmailData> userEmails = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    @ToString.Exclude
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Account userAccount;

    @Version
    private Long version;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
