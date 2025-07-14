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

    // --- Метод для модификации (WRITE) ---
    // ЗАДАЧА: Получить полную, "жирную" сущность и заблокировать ее для изменений
    // РЕШЕНИЕ: JPQL с JOIN/LEFT JOIN FETCH и PESSIMISTIC_WRITE блокировкой
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT c FROM Client c " +
            "LEFT JOIN FETCH c.personalAccounts " +// Явно загружаем всё, что может понадобиться для изменения
            "LEFT JOIN FETCH c.sharedAccountMemberships " +
            "LEFT JOIN FETCH c.roles " +
            "LEFT JOIN FETCH c.userEmails " +
            "LEFT JOIN FETCH c.userPhones " +
            "WHERE c.id = :id")
    Optional<Client> getFullById(@Param("id") Long id);

    @Query("""
        FROM Client c WHERE c.id = :userId
           """)
    Optional<Client> findClientById(@Param("userId") Long userId);

    @Query(value = """
                   SELECT EXISTS(SELECT TRUE FROM business.users WHERE users.id = :referrerId)
                   """, nativeQuery = true)
    boolean checkClientExistenceInDb(@Param("referrerId") Long refererId);
}