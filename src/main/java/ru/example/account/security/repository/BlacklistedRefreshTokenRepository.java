package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.BlackListedRefreshToken;

@Repository
public interface BlacklistedRefreshTokenRepository extends JpaRepository<BlackListedRefreshToken, String> {

    @Query(value = """
                   SELECT EXISTS(SELECT 1
                   FROM security.black_list_refresh_tokens AS blrt
                   WHERE blrt.token = :refreshToken)
                   """, nativeQuery = true)
    boolean existsByRefreshToken(@Param("refreshToken") String refreshToken);
}
