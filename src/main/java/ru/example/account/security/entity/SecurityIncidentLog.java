package ru.example.account.security.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.proxy.HibernateProxy;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "security_incidents_log", schema = "security")
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SecurityIncidentLog {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private IncidentType incidentType;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "session_id")
    private UUID sessionId;

    // --- СВЯЗЬ One-to-Many ("ОДИН Инцидент -> МНОГО Деталей") ---
    @OneToMany(
            mappedBy = "incident",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<SecurityIncidentDetail> details = new HashSet<>();

    @Column(name = "incident_timestamp", nullable = false)
    private ZonedDateTime incidentTimestamp;

    @Column(nullable = false)
    @Builder.Default
    private String status = "DETECTED";

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        SecurityIncidentDetail that = (SecurityIncidentDetail) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    // --- "ЧИСТЫЙ" МЕТОД-ХЕЛПЕР ---
    public void addDetail(String key, String value) {
        SecurityIncidentDetail detail = new SecurityIncidentDetail();
        // "Родитель" САМ устанавливает связь
        detail.setIncident(this);
        detail.setDetailKey(key);
        detail.setDetailValue(value);
        this.details.add(detail);
    }

    // Удобный хелпер для добавления множества "улик"
    public void addDetails(Map<String, String> detailsMap) {
        if (detailsMap == null) return;
        detailsMap.forEach(this::addDetail);
    }
}