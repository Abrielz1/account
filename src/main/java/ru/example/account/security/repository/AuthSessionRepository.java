package ru.example.account.security.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status);

    @Query(value = """
                   FROM AuthSession au
                   WHERE au.userId = :userId AND au.status = :sessionStatus
                   """)
    List<AuthSession> findAllByUserIdAndStatus(@Param("userId") Long userId, @Param("sessionStatus") SessionStatus sessionStatus);

    @Query("""
            SELECT EXISTS (SELECT 1 FROM AuthSession auth  WHERE auth.id = :sessionId)
            """)
    boolean checkSessionIdAuditLog(@Param("sessionId") String sessionId);

    Boolean existsByRefreshToken(String token);

    Boolean existsByFingerprint(String fingerprintHash);

    @Query(value = """
                   """, nativeQuery = true)
    Slice<AuthSession> getAllActiveSession(@Param("cursor") Long cursor,
                                           @Param("sessionId") UUID sessionId,
                                           @Param("revocationStatus") RevocationReason revocationReason,
                                           Pageable pageRequest);
}
