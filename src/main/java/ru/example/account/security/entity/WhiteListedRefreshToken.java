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
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Table(name = "white_listed_refresh_tokens", schema = "security")
@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WhiteListedRefreshToken {

    // Сам токен - это и есть первичный ключ
    @Id
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(name = "fingerprint_hash", columnDefinition = "TEXT", nullable = false)
    private String fingerprintHash;

    // Время, когда этот токен был СОЗДАН
    // (мы возьмем его из expiresAt - ttl)
    @Column(nullable = false)
    private Instant createdAt;

    // Время, когда этот токен должен был ИСТЕЧЬ
    @Column(name = "original_expiry_date", nullable = false)
    private Instant originalExpiryDate;

    // Время, когда он был реально ОТОЗВАН
    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // Причина отзыва
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RevocationReason reason;

    private void setToken(String token) {
        this.token = token;
    }

    private void setUserId(Long userId) {
        this.userId = userId;
    }

    private void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    private void setFingerprintHash(String fingerprintHash) {
        this.fingerprintHash = fingerprintHash;
    }

    private void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    private void setOriginalExpiryDate(Instant originalExpiryDate) {
        this.originalExpiryDate = originalExpiryDate;
    }

    private void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    private void setReason(RevocationReason reason) {
        this.reason = reason;
    }

    public void revoke(RevocationReason revocationReason, SessionStatus sessionStatus) {

        if (!Objects.equals(SessionStatus.STATUS_ACTIVE, sessionStatus)) {
            log.warn("[WARN] session MUST be Active to coorect logout!");
            throw new RuntimeException();
        }

        this.setRevokedAt(Instant.now());
        this.setIsActive(false);
        this.setReason(revocationReason);
    }
}
