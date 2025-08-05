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

    Optional<AuthSession> findByAccessTokenAndStatus(String accessToken, SessionStatus status);

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

    /**
     * Получает HMAC-хэш фингерпринта из АКТИВНОЙ сессии по ее refresh-токену.
     * Возвращает ТОЛЬКО одну строку.
     * @param refreshToken "Сырой" refresh-токен.
     * @return Optional, содержащий хэш, если сессия найдена.
     */
    @Query(value = "SELECT fingerprint_hash FROM security.auth_sessions " +
            "WHERE refresh_token = :token AND status = 'STATUS_ACTIVE'",
            nativeQuery = true)
    Optional<String> findFingerprintHashByActiveRefreshToken(@Param("token") String refreshToken);

    /**
     * Получает "сырой" фингерпринт из АКТИВНОЙ сессии по ее refresh-токену.
     * Возвращает ТОЛЬКО одну строку.
     * @param refreshToken "Сырой" refresh-токен.
     * @return Optional, содержащий "сырой" фингерпринт, если сессия найдена.
     */
    @Query(value = "SELECT fingerprint FROM security.auth_sessions " +
            "WHERE refresh_token = :token AND status = 'STATUS_ACTIVE'",
            nativeQuery = true)
    Optional<String> findOriginalFingerprintByActiveRefreshToken(@Param("token") String refreshToken);
}
