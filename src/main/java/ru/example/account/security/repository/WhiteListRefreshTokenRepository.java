package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.WhiteListedRefreshToken;
import java.util.Optional;

@Repository
public interface WhiteListRefreshTokenRepository extends JpaRepository<WhiteListedRefreshToken, String> {

    Optional<WhiteListedRefreshToken> findByToken(String refreshToken);

    boolean existsByToken(String refreshToken);
}
