package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.WhiteListedAccessesToken;

@Repository
public interface WhiteListAccessTokenRepository extends JpaRepository<WhiteListedAccessesToken, String> {

    boolean existsByToken(String accessToken);
}
