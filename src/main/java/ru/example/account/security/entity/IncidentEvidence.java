package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import ru.example.account.shared.util.AesCryptoConverter;

@Table(name = "incident_evidence", schema = "security")
@Entity
@Getter
@Setter
@Builder
@Where(clause = "is_deleted = false")
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // СВЯЗЬ С "ХОЗЯИНОМ". Этот "вещдок" НЕ МОЖЕТ существовать без "дела".
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private SecurityIncidentDetail incident;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false)
    private BlockedEntityType evidenceType;

    // ЗАШИФРОВАННАЯ УЛИКА
    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "evidence_value", columnDefinition = "TEXT", nullable = false)
    private String evidenceValue;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}
