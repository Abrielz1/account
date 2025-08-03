package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.RevokedSessionArchive;

@Repository
public interface RevokedTokenArchiveRepository extends JpaRepository<RevokedSessionArchive, String> {

    @Query(value = """
           SELECT EXISTS (SELECT 1 FROM RevokedSessionArchive rsa WHERE rsa.refreshToken = :refreshToken)
           """)
    Boolean checkRefreshTokenInRevokedArchive(@Param("refreshToken") String refreshToken);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM security.revoked_sessions_archive WHERE access_token = :token)",
            nativeQuery = true)
    boolean existsByAccessToken(@Param("token") String accessToken);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM security.revoked_sessions_archive WHERE refresh_token = :token)",
            nativeQuery = true)
    boolean existsByRefreshToken(@Param("token") String refreshToken);
}

