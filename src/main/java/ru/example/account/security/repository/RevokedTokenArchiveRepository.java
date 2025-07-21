package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.RevokedSessionArchive;

@Repository
public interface RevokedTokenArchiveRepository extends JpaRepository<RevokedSessionArchive, String> {


}

