package ru.example.account.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.user.entity.EmailData;

@Repository
public interface EmailDataRepository extends JpaRepository<EmailData, Long> {
}
