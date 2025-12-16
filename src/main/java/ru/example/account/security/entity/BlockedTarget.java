package ru.example.account.security.entity;

import jakarta.persistence.Column;
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
import ru.example.account.security.entity.enums.BlockedEntityType;

import java.time.ZonedDateTime;
import java.util.UUID;

@Table(name = "blocked_targets", schema = "security")
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

    // null = навсегда. Дата = "серый список", те временно
    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    @Column(name = "affected_session_id")
    private UUID affectedSessionId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggering_incident_id")
    private SecurityIncident triggeringIncident;
}
