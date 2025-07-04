package ru.example.account.user.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.user.entity.User;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 * Поддерживает пессимистичные блокировки и кэширование.
 */
@Repository
@CacheConfig(cacheNames = "users")
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000")})
    @EntityGraph()
    @Query("SELECT u FROM User u INNER JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> getWithRolesByEmail(@Param("username") String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userAccount " + // Явно загружаем всё, что может понадобиться для изменения
            "LEFT JOIN FETCH u.roles " +
            "LEFT JOIN FETCH u.userEmails " +
            "LEFT JOIN FETCH u.userPhones " +
            "WHERE u.id = :id")
    Optional<User> getFullById(@Param("id") Long id);

    boolean existsByUsername(String username);
    boolean existsByUserEmails_Email(String email);
    boolean existsByUserPhones_Phone(String phone);
}
