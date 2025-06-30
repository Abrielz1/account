package ru.example.account.app.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.app.entity.Account;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(value = """
    FROM Account AS a
    WHERE a.balance < (:maxPercent * a.initialBalance)
    """)
    @QueryHints({
            @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
   // @Lock(LockModeType.PESSIMISTIC_WRITE) // <-- Проблема №1
    Page<Account> findAllNotBiggerThanMax(@Param("maxPercent") BigDecimal maxPercent, Pageable pageable);

    @Query("SELECT u.userAccount.id FROM User u WHERE u.id = :userId")
    Optional<Long> findAccountIdByUserIdSafe(@Param("userId") Long userId);

    @Query(value = """
       SELECT a FROM Account AS a WHERE a.id = :accountId
""")
  @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000")
    })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> getAccountWithLocksByUserId(@Param("accountId") Long accountId);

    @Query("""
                   FROM Account AS a
                   WHERE a.id > :lastProcessedId AND  a.balance < (a.initialBalance * :maxPercent)
                   ORDER BY a.id
                   """)
    Slice<Account> getNextBatch(@Param("lastProcessedId") Long lastProcessedId,
                                @Param("maxPercent") BigDecimal maxPercent,
                                Pageable pageable);
}
