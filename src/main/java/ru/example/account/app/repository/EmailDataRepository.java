package ru.example.account.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.app.entity.EmailData;

@Repository
public interface EmailDataRepository extends JpaRepository<EmailData, Long> {
}
