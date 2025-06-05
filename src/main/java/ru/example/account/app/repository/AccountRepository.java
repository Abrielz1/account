package ru.example.account.app.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.app.entity.Account;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Cacheable(key = "#accountId", unless = "#result == null")
    @Query("SELECT a FROM Account a WHERE a.id = :accountId")
    Optional<Account> findAccountById(@Param("accountId") Long accountId);

    @Query(value = """
    FROM Account AS a
    WHERE a.balance < (:maxPercent * a.initialBalance)
    """)
    @QueryHints({
    @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
    @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Cacheable(value = "accounts", key = "#maxPercent.toString()", unless = "#result.isEmpty()")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Account> findAllNotBiggerThanMax(@Param("maxPercent") BigDecimal maxPercent);
}
