package ru.example.account.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.user.entity.EmailData;

@Repository
public interface EmailDataRepository extends JpaRepository<EmailData, Long> {


    // --- Методы для быстрой проверки на существование ---
    // ЗАДАЧА: Максимально быстро проверить, занято ли поле
    // РЕШЕНИЕ: Native Query с SELECT EXISTS
    @Query(value = """
    SELECT EXISTS(SELECT 1 FROM business.email_data WHERE business.email_data.email = :email)
                   """, nativeQuery = true)
    boolean checkUserByEmail(@Param("email") String email);
}
