package ru.example.account.app.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
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
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Cacheable(key = "#username", unless = "#result.empty")
    Optional<User> findByUserEmails(String username);

    /**
     * Поиск пользователя с блокировкой записи.
     *
     * @Lock(LockModeType.PESSIMISTIC_WRITE) Гарантирует эксклюзивный доступ
     * @Cacheable Результат кэшируется на 30 минут
     */
    @QueryHints(@QueryHint(name = "javax.persistence.cache.retrieveMode", value = "BYPASS"))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findWithLockingById(@Param("id") Long id);


    @Query(value = """
          SELECT CASE WHEN COUNT(ue) > 0 THEN TRUE ELSE FALSE END
          FROM User AS u JOIN u.userEmails AS ue
          WHERE ue.email = :email
          """)
    boolean existsEmails(@Param("email") String email);


    @Query(value = """
          SELECT CASE WHEN COUNT(up) > 0 THEN TRUE  ELSE FALSE END
          FROM User AS u JOIN u.userPhones AS up
          WHERE up.phone = :phone
          """)
    boolean existsByPhones(@Param("phone")String phone);

    @Query(value = """
                   FROM User  AS u JOIN u.userEmails AS ue
                   WHERE ue.email = :email
                   """)
    Optional<User> findByEmail(@Param("email") String email);
}
