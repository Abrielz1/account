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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
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

    // --- "Слоновьи" equals/hashCode, основанные на PK ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityIncidentLog that = (SecurityIncidentLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // --- ТВОЙ "ЧИСТЫЙ" МЕТОД-ХЕЛПЕР ---
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