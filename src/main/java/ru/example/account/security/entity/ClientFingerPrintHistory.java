package ru.example.account.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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
import lombok.ToString;
import org.hibernate.annotations.Where;
import ru.example.account.shared.util.AesCryptoConverter;
import java.time.ZonedDateTime;

@Entity
@Table(name = "client_fingerprints_history", schema = "security")
@Getter
@Setter
@Builder
@Where(clause = "is_deleted = false")
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "userId", "fingerprint"})
public class ClientFingerPrintHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fingerprint_hash", nullable = false, updatable = false, unique = true)
    // ЭТО ПОЛЕ ДЛЯ ПОИСКА. ОНО НЕ ШИФРУЕТСЯ.
    private String fingerprintHash;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "fingerprint", nullable = false)
    String  fingerprint;

    @Column(name = "first_seen_at", nullable = false)
    ZonedDateTime firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    ZonedDateTime lastSeenAt;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "user_agent")
    private String userAgent;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "ip_address")
    private String ipAddress;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "location_geo_info")
    String locationGeoInfo;

    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "user_agent")
    String userAgentInfo;

    @Column(name = "is_trusted")
    Boolean isTrusted;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}
