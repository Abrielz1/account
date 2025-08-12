package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.UserFingerprintProfile;

import java.util.Optional;

/**
 * Репозиторий для управления "родительской" сущностью - профилями фингерпринтов.
 * Используется в основном для "пишущих" операций (создание/обновление профиля)
 * и для загрузки полного профиля со всеми дочерними фингерпринтами.
 */
@Repository
public interface UserFingerprintProfileRepository extends JpaRepository<UserFingerprintProfile, Long> {

    @Query(value = """
                   SELECT *
                   FROM security.user_fingerprint_profiles AS ufpp
                   WHERE ufpp.user_id = :userId
                   """, nativeQuery = true)
    Optional<UserFingerprintProfile> findByUserId(@Param("userId") Long userId);
}