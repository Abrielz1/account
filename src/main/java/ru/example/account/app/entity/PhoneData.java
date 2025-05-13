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

@Table(name = "phone_data")
@Entity
@Getter
@Setter
@Builder
@ToString
@NamedEntityGraph(
        name = "account-with-phone",
        attributeNodes = @NamedAttributeNode("user")
)
@NoArgsConstructor
@AllArgsConstructor
public class PhoneData {

    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", unique = true)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private User user;

    @Version
    private Long version;
}
