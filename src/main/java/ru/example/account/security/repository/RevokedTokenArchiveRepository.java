package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.RevokedTokenArchive;

@Repository
public interface RevokedTokenArchiveRepository extends JpaRepository<RevokedTokenArchive, String> {


}

