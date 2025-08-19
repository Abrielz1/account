package ru.example.account.security.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Table(name = "security_incidents", schema = "security")
@Entity
@Getter
@Setter
@Builder
@EqualsAndHashCode
@Where(clause = "is_deleted = false") // todo исправить
@NoArgsConstructor
@AllArgsConstructor
public class SecurityIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private IncidentType type; // TOKEN_REPLAY, FINGERPRINT_MISMATCH и т.д.

    @Column(name = "timestamp", nullable = false)
    private ZonedDateTime timestamp;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary; // Короткое описание для админов: "Replay attack from IP 1.2.3.4 for user 123"

    //СЛЕПКИ УЧАСТНИКОВ НА МОМЕНТ ИНЦИДЕНТА
    @Column(name = "involved_user_id")
    private Long involvedUserId;

    @Column(name = "source_session_id")
    private UUID sourceSessionId;

    @Column(name = "source_ip_address")
    private String sourceIpAddress;

    @Column(name = "source_fingerprint_hash")
    private String sourceFingerprintHash;

    @Column(name = "status")
    @Builder.Default
    private String status = "DETECTED";

    // ОДИН Инцидент ("Дело") ВЛАДЕЕТ МНОЖЕСТВОМ "Вещдоков".
    // ОН -- "Агрегатный корень". При удалении "Дела" -- удаляются и все "Вещдоки".
    @OneToMany(
            mappedBy = "incident", // "Я -- главный в этой связи, ищите foreign key ТАМ"
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<IncidentEvidence> evidences = new HashSet<>();

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}
