package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.RefreshToken;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenRefresh(String token);

    void deleteByUserId(Long userId);

    List<RefreshToken> findAllByUserId(Long userId);

    boolean existsByToken(String token);
}
