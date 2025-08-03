package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.UserFingerprintProfile;

/**
 * Репозиторий для управления "родительской" сущностью - профилями фингерпринтов.
 * Используется в основном для "пишущих" операций (создание/обновление профиля)
 * и для загрузки полного профиля со всеми дочерними фингерпринтами.
 */
@Repository
public interface UserFingerprintProfileRepository extends JpaRepository<UserFingerprintProfile, Long> {


}