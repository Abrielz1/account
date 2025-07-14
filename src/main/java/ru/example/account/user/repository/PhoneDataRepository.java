package ru.example.account.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.user.entity.PhoneData;

@Repository
public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {


    @Query(value = """
            SELECT EXISTS(SELECT 1 FROM business.phone_data WHERE business.phone_data.phone = :phone)
            """, nativeQuery = true)
    boolean checkUserByPhone(@Param("phone") String phone);
}
