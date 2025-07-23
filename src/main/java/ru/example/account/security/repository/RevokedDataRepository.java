package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.RevokedClientData;
import java.util.UUID;

@Repository
public interface RevokedDataRepository extends JpaRepository<RevokedClientData, UUID> {
}
