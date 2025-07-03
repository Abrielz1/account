package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "revoked_tokens_archive", schema = "security")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokedTokenArchive {

    @Id
    private String token; // Cам refresh-токен

    @Column(nullable = false, unique = true)
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    @Column(nullable = false)
    private String reason;
}