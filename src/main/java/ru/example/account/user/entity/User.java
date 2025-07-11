package ru.example.account.user.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
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
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.example.account.business.entity.Account;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import ru.example.account.business.entity.AccountMembership;

@NamedEntityGraph(
        name = "User.withRoles",
        attributeNodes = @NamedAttributeNode("roles")
)

@NamedEntityGraph(
        name = "User.withAllDetails",
        attributeNodes = {
                @NamedAttributeNode(value = "personalAccounts"), // Теперь тащим персональные счета
                @NamedAttributeNode(value = "sharedAccountMemberships", subgraph = "membership-details"),
                @NamedAttributeNode("roles"),
                @NamedAttributeNode("userEmails"),
                @NamedAttributeNode("userPhones")
        },
        subgraphs = {
                // Сабграф нужен, чтобы из AccountMembership подтянуть сам Account
                @NamedSubgraph(name = "membership-details", attributeNodes = @NamedAttributeNode("account"))
        }
)

@Entity
@Table(name = "users", schema = "business")
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"id", "username"})
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Version
    private Long version = 0L;

    @ElementCollection(targetClass = RoleType.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", schema = "business", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    @ToString.Exclude
    private Set<RoleType> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<EmailData> userEmails = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<PhoneData> userPhones = new HashSet<>();

    // --- СВЯЗЬ №1: Персональные счета (простая One-to-Many) ---
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Account> personalAccounts = new HashSet<>();

    // --- СВЯЗЬ №2: Участие в общих счетах (сложная Many-to-Many) ---
    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private Set<AccountMembership> sharedAccountMemberships = new HashSet<>();

    @Transient // <-- Говорим Hibernate игнорировать это поле
    public Set<Account> getAccounts() {

        if (sharedAccountMemberships == null) {
            return new HashSet<>();
        }
        return sharedAccountMemberships.stream()
                .map(AccountMembership::getAccount)
                .collect(Collectors.toSet());
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    public void addRole(RoleType role) {
        this.roles.add(role);
    }

    public void addEmail(String email) {
        EmailData newEmail = new EmailData(null, email, this, null);
        this.userEmails.add(newEmail);
    }

    public void addPhone(String  phone) {
        PhoneData newPhone = new PhoneData(null, phone, this, null);
        this.userPhones.add(newPhone);
    }
}