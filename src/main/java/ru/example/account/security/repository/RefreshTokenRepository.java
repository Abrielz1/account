package ru.example.account.security.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.ActiveSessionCache;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<ActiveSessionCache, Long> {

    Optional<ActiveSessionCache> findByTokenRefresh(String token);

    void deleteByUserId(Long userId);

    List<ActiveSessionCache> findAllByUserId(Long userId);

    boolean existsByToken(String token);
}
