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
    @Column(name = "refres_token_value", nullable = false) // <-- Переименовал, чтобы не конфликтовать с "token"
    private String refreshTokenValue;

    @Column(name = "accesses_token_value", nullable = false)
    private String accessesTokenValue;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    @Column(name = "fingerprint", nullable = false)
    private String fingerprint;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status",nullable = false) // статус сессии перед и после архивации
    @JdbcType(PostgreSQLEnumJdbcType.class) // Маппинг на кастомный ENUM
    private SessionStatus sessionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason",nullable = false) //Причина прилёта мухобойки
    @JdbcType(PostgreSQLEnumJdbcType.class) // Маппинг на кастомный ENUM
    private RevocationReason reason;
}