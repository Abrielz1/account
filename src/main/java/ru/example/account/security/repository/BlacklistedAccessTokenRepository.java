package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.BlacklistedAccessToken;

@Repository
public interface BlacklistedAccessTokenRepository  extends JpaRepository<BlacklistedAccessToken, String> {

}
