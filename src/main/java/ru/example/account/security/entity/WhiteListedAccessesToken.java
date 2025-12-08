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
@Table(name = "white_listed_access_tokens", schema = "security")
@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WhiteListedAccessesToken {

    /**
     * Сам Access-токен. Является первичным ключом для мгновенного поиска.
     */
    @Id
    @Column(name = "token")
    private String token;

    /**
     * ID пользователя, которому принадлежал этот токен.
     * Критически важно для расследований инцидентов.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * ID сессии, в рамках которой был выпущен этот токен.
     * Позволяет связать скомпрометированный токен с конкретной сессией.
     */
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    /**
     * Время, когда этот токен был СОЗДАН.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "fingerprint_hash", columnDefinition = "TEXT", nullable = false)
    private String fingerprintHash;

    /**
     * Изначальное время жизни токена. Показывает, когда он должен был истечь.
     */
    @Column(name = "original_expiry_date", nullable = false)
    private Instant originalExpiryDate;

    /**
     * Точное время, когда токен был отозван.
     */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * Причина, по которой токен был отозван.
     * Позволяет понять, был ли это штатный выход или "Красная тревога".
     */
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

    private void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    private void setFingerprintHash(String fingerprintHash) {
        this.fingerprintHash = fingerprintHash;
    }

    private void setOriginalExpiryDate(Instant originalExpiryDate) {
        this.originalExpiryDate = originalExpiryDate;
    }

    private void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    private void setActive(Boolean active) {
        isActive = active;
    }

    private void setReason(RevocationReason reason) {
        this.reason = reason;
    }

    public void revoke(String accessToken, RevocationReason revocationReason, SessionStatus sessionStatus) {

        if (!Objects.equals(SessionStatus.STATUS_ACTIVE, sessionStatus)) {
            log.warn("[WARN] session MUST be Active to coorect logout!");
            throw new RuntimeException();
        }

        this.setToken(accessToken);
        this.setRevokedAt(Instant.now());
        this.setIsActive(false);
        this.setReason(revocationReason);
    }
}
