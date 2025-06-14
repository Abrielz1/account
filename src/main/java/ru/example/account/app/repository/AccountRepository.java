package ru.example.account.app.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.app.entity.Account;
import java.math.BigDecimal;

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
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Page<Account> findAllNotBiggerThanMax(@Param("maxPercent") BigDecimal maxPercent, Pageable pageable);
}
