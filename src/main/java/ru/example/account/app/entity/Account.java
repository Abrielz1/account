package ru.example.account.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import java.math.BigDecimal;

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
public class Account {
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "balance", precision = 19, scale = 2)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private User user;

    @Version
    private Long version;
}
