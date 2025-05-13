package ru.example.account.app.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.app.entity.RefreshToken;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenRefresh(String token);

    void deleteByUserId(Long userId);
}
