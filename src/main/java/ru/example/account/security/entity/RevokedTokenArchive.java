package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "revoked_tokens_archive", schema = "security") // Кладем в security-схему
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokedTokenArchive {
    @Id
    private String token;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class) // Маппинг на кастомный ENUM
    private RevocationReason reason;
}