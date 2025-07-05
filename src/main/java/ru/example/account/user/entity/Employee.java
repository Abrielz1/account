package ru.example.account.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "employees", schema = "business")
@DiscriminatorValue("EMPLOYEE") // Указывает, какое значение будет в колонке user_type
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = {"mentor", "mentees"}) // callSuper=true, чтобы в toString
public class Employee extends User {

    @Column(name = "employee_internal_id", unique = true)
    private String employeeInternalId;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "department")
    private String department;

    @Column(name = "position")
    private String position;

    @Column(name = "security_cleared", nullable = false)
    private boolean isSecurityCleared = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    @ToString.Exclude
    private Employee mentor;

    @OneToMany(mappedBy = "mentor")
    @ToString.Exclude
    private Set<Employee> mentees = new HashSet<>();

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;

            Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
            Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();

            if (thisEffectiveClass != oEffectiveClass) return false;

            Employee employee = (Employee) o;

            return getId() != null && Objects.equals(getId(), employee.getId());
        }

        @Override
        public final int hashCode() {
            // Мы используем хэш-код "настоящего" класса, а не прокси.
            Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
            return thisEffectiveClass.hashCode();
        }
}
