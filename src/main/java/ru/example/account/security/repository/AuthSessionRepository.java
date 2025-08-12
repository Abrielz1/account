package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.SessionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByAccessTokenAndStatus(String accessToken, SessionStatus status);  // todo nativeQuery

    Optional<AuthSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status); // todo nativeQuery

    @Query(value = """
                   SELECT *
                   FROM security.auth_sessions AS au
                   WHERE au.userId = :userId
                    AND au.status = :sessionStatus
                   """, nativeQuery = true)
    List<AuthSession> findAllByUserIdAndStatus(@Param("userId") Long userId, @Param("sessionStatus") SessionStatus sessionStatus);

    @Query(value = """
            SELECT EXISTS (SELECT 1 FROM security.auth_sessions AS au WHERE au.id = :sessionId)
            """, nativeQuery = true)
    boolean checkSessionIdAuditLog(@Param("sessionId") String sessionId);

    @Query(value = """
           SELECT EXISTS(SELECT 1 FROM security.auth_sessions AS au WHERE au.token = :token)
           """, nativeQuery = true)
    Boolean existsByRefreshToken(@Param("token") String token); // todo native querry

    /**
     * Получает HMAC-хэш фингерпринта из АКТИВНОЙ сессии по ее refresh-токену.
     * Возвращает ТОЛЬКО одну строку.
     * @param refreshToken "Сырой" refresh-токен.
     * @return Optional, содержащий хэш, если сессия найдена.
     */
    @Query(value = """
            SELECT *
             FROM security.auth_sessions AS au
            WHERE au.refresh_token = :token
             AND au.status = 'STATUS_ACTIVE'
            """, nativeQuery = true)
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

    @Query(value = """
            SELECT *
             FROM security.auth_sessions AS au
            WHERE au.user_id = :userId
             AND au.status = 'STATUS_ACTIVE'
            """, nativeQuery = true)
    Optional<AuthSession> findBySessionIdAndUserIdAndStatusActive(@Param("sessionId") UUID sessionId, @Param("userId") Long userId);
}
