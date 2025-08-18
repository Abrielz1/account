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
import org.hibernate.validator.constraints.UUID;
import java.time.Instant;

/**
 * Персистентный, "холодный" черный список ACCESS-токенов.
 * Служит "источником правды" и страховкой на случай падения Redis.
 * Запись сюда означает, что токен скомпрометирован и не может быть использован НИКОГДА.
 */
@Table(name = "black_list_access_tokens", schema = "security")
@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedAccessToken {

    /**
     * Сам Access-токен. Является первичным ключом для мгновенного поиска.
     */
    @Id
    private String token;

    /**
     * ID пользователя, которому принадлежал этот токен.
     * Критически важно для расследований инцидентов.
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * ID сессии, в рамках которой был выпущен этот токен.
     * Позволяет связать скомпрометированный токен с конкретной сессией.
     */
    @Column(nullable = false)
    private UUID sessionId;

    /**
     * Время, когда этот токен был СОЗДАН.
     */
    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Изначальное время жизни токена. Показывает, когда он должен был истечь.
     */
    @Column(name = "original_expiry_date", nullable = false)
    private Instant originalExpiryDate;

    /**
     * Точное время, когда токен был помещен в черный список.
     */
    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    /**
     * Причина, по которой токен был отозван.
     * Позволяет понять, был ли это штатный выход или "Красная тревога".
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RevocationReason reason;
}
