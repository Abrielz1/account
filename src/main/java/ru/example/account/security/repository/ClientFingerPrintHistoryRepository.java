package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.ClientFingerPrintHistory;

@Repository
public interface ClientFingerPrintHistoryRepository extends JpaRepository<ClientFingerPrintHistory, Long> {

    @Query(value = """
    SELECT EXISTS(SELECT 1 FROM ClientFingerPrintHistory cfh WHERE cfh.fingerprintHash = :fingerPrintHashToCheck)
""")
    Boolean checkExistsClientFingerprint(@Param("fingerPrintHashToCheck") String fingerPrintHashToCheck);
}
