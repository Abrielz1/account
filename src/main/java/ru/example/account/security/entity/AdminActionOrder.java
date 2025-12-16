package ru.example.account.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.example.account.security.entity.enums.AdminActionOrderBasis;
import ru.example.account.security.entity.enums.AdminActionOrderType;
import ru.example.account.shared.util.AesCryptoConverter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_action_orders", schema = "security")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionOrder {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AdminActionOrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AdminActionOrderBasis basisType;

    @Convert(converter = AesCryptoConverter.class)
    private String basisDocumentRef;

    private Long targetUserId;

    private Long targetAccountId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Long createdByEmployeeId;

    @Column(nullable = false)
    private Instant createdAt;

    private Long approvedByEmployeeId;

    private Instant executedAt;

    @Convert(converter = AesCryptoConverter.class)
    private String notes;

}
