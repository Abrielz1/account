package ru.example.account.app.repository;

import jakarta.persistence.LockModeType;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.app.entity.User;
import java.util.Optional;
/**
 * Репозиторий для работы с пользователями.
 * Поддерживает пессимистичные блокировки и кэширование.
 */
@Repository
@CacheConfig(cacheNames = "users")
public interface UserRepository extends JpaRepository<User, Long> {

    @Cacheable(key = "#username", unless = "#result == null")
    Optional<User> findByUsername(String username);
    /**
     * Поиск пользователя с блокировкой записи.
     *
     * @Lock(LockModeType.PESSIMISTIC_WRITE) Гарантирует эксклюзивный доступ
     * @Cacheable Результат кэшируется на 30 минут
     */
    @Cacheable(value = "users", key = "#id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findWithLockingById(@Param("id") Long id);
}
