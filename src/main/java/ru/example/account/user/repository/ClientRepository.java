package ru.example.account.user.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.user.entity.Client;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 * Поддерживает пессимистичные блокировки и кэширование.
 */
@Repository
@CacheConfig(cacheNames = "clients")
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    // --- Метод для аутентификации (READ) ---
    // ЗАДАЧА: Быстро и надежно получить User + Roles
    // РЕШЕНИЕ: JPQL с INNER JOIN FETCH и PESSIMISTIC_READ блокировкой
    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000")})
    @Query("SELECT с FROM Client с INNER JOIN FETCH с.roles WHERE с.userEmails = :email")
    Optional<Client> getWithRolesByEmail(@Param("email") String email);

    // --- Метод для модификации (WRITE) ---
    // ЗАДАЧА: Получить полную, "жирную" сущность и заблокировать ее для изменений
    // РЕШЕНИЕ: JPQL с JOIN/LEFT JOIN FETCH и PESSIMISTIC_WRITE блокировкой
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT с FROM Client с " +
            "LEFT JOIN FETCH с.personalAccounts " + // Явно загружаем всё, что может понадобиться для изменения
            "LEFT JOIN FETCH с.roles " +
            "LEFT JOIN FETCH с.userEmails " +
            "LEFT JOIN FETCH с.userPhones " +
            "WHERE с.id = :id")
    Optional<Client> getFullById(@Param("id") Long id);

    // --- Методы для быстрой проверки на существование ---
    // ЗАДАЧА: Максимально быстро проверить, занято ли поле
    // РЕШЕНИЕ: Native Query с SELECT EXISTS
    @Query(value = """
    SELECT EXISTS(SELECT 1 FROM business.email_data WHERE business.email_data.email = :email)
                   """, nativeQuery = true)
    boolean checkUserByEmail(@Param("email") String email);

    @Query(value = """
    SELECT EXISTS(SELECT 1 FROM business.users WHERE business.users.username = :username)
            """,nativeQuery = true)
    boolean checkUserByUsername(@Param("username") String username);

    @Query(value = """
            SELECT EXISTS(SELECT 1 FROM business.phone_data WHERE business.phone_data.phone = :phone)
            """, nativeQuery = true)
    boolean checkUserByPhone(@Param("phone") String phone);

    @Query("""
        FROM User u WHERE u.id = :userId
""")
    Optional<Client> getByID(@Param("userId") Long userId);
}
