package ru.example.account.user.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.user.entity.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    // --- Метод для аутентификации (READ) ---
    // ЗАДАЧА: Быстро и надежно получить User + Roles
    // РЕШЕНИЕ: JPQL с INNER JOIN FETCH и PESSIMISTIC_READ блокировкой
    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000")})
    @Query("SELECT с FROM Client с INNER JOIN FETCH с.roles WHERE с.userEmails = :email")
    Optional<User> getWithRolesByEmail(@Param("email") String email);

    @Query(value = """
    SELECT EXISTS(SELECT TRUE FROM business.users WHERE users.username = :Username)             
                   """, nativeQuery = true)
    Boolean checkUserByUsername(@Param("Username") String Username);
}
