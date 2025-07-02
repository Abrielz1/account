package ru.example.account.business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "roles", schema = "business")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    private Short id;

    @Column(nullable = false, unique = true)
    private String name;

//    @OneToMany(mappedBy = "role")
//    private Set<UserRole> userRoles = new HashSet<>();

}