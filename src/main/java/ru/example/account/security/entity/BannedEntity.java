package ru.example.account.security.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.example.account.shared.util.AesCryptoConverter;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "blocked_entities", schema = "security")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"blockReason"})
public class BannedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private BlockedEntityType entityType; // Новый ENUM: 'IP_ADDRESS', 'USER_ID', 'FINGERPRINT'

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "entity_value", nullable = false, columnDefinition = "TEXT")
    private String entityValue; // Здесь будет лежать сам IP (1.2.3.4), или User ID (123), или хэш

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant blockedAt;

    @Column(name = "expires_at")
    private Instant expiresBanAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ip_adresses")
    @OneToMany(mappedBy = "BannedEntity")
    private Set<BlackListedIpAddress> listOfBlockedIps;

    @Convert(converter = AesCryptoConverter.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "block_reason")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private BlockReason blockReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private BlockType blockType;

    @Column(name = "banned_by_employee_id")
    private Long bannedByEmployeeId; // Ссылка на сотрудника, кто забанил (может быть null для автобана)

    @Column(name = "banned_user_id") // Можно хранить ID, а не полную связь
    private Long bannedUserId; // Если банили конкретного юзера, сохраняем его ID.

    @OneToMany(mappedBy = "ban", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BanContextDetail> contextDetails;

    @OneToMany(mappedBy = "blockedEntity")
    private Set<BlackListedIpAddress> blackListedIpAddresses;

    // Хелпер, как ты любишь
    public void addDetail(String key, String value) {

        // 1. Создаем "пустого" ребенка
        BanContextDetail detail = new BanContextDetail();

        // 2. "Родитель" САМ, своей "рукой", устанавливает связь
        detail.setBan(this); // <<<--- ГЛАВНАЯ МАГИЯ!

        // 3. Заполняем остальные поля
        detail.setDetailKey(key);
        detail.setDetailValue(value);

        // 4. "Усыновляем" ребенка
        this.contextDetails.add(detail);
    }
}