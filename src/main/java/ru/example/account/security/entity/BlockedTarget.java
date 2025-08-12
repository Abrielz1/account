package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import java.time.ZonedDateTime;
import java.util.UUID;

@Table(name = "blocked_target", schema = "security")
@Entity
@Getter
@Setter
@Builder
@Where(clause = "is_deleted = false")
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BlockedTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private BlockedEntityType targetType; // ENUM: IP_ADDRESS, FINGERPRINT, USER_ID...

    @Column(name = "target_value", nullable = false)
    private String targetValue;

    @Column(name = "affected_user_id")
    private Long affectedUserId;

    @Column(name = "blocked_by_user_id")
    private Long blockedByUserId;

    // null = навсегда. Дата = "серый список".
    @Column(name = "expires_at") // null = навсегда. Дата = временно.
    private ZonedDateTime expiresAt;

    @Column(name = "affected_session_id   ")
    private UUID affectedSessionId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "triggering_incident_id")
    private UUID triggeringIncidentId; // Здесь UUID, потому что Incidents его могут использовать в будущем

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}
