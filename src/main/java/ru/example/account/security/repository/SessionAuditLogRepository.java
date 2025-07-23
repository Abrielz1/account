package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.SessionAuditLog;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionAuditLogRepository  extends JpaRepository<SessionAuditLog, UUID> {


    @Query("""
            SELECT EXISTS (SELECT 1 FROM SessionAuditLog sal WHERE sal.sessionId = :sessionId)
            """)
    boolean checkSessionIdAuthSession(@Param("sessionId") String sessionId);

    Optional<SessionAuditLog> findBySessionId(UUID id);
}
