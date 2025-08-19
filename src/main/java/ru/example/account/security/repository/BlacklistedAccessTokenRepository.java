package ru.example.account.security.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.BlacklistedAccessToken;

@Repository
public interface BlacklistedAccessTokenRepository  extends JpaRepository<BlacklistedAccessToken, String> {

    @Query(value = """
                   SELECT EXISTS(SELECT 1
                   FROM security.black_list_access_tokens AS blat
                   WHERE blat.token = :accessToken)
                   """, nativeQuery = true)
    boolean existsByAccessToken(@Param("accessToken") String accessToken);
}
