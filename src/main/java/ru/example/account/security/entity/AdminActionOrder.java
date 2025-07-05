package ru.example.account.security.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.example.account.security.entity.AdminActionOrderBasis;
import ru.example.account.security.entity.AdminActionOrderType;

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

    private String notes;

}
