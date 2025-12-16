package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.BannedEntity;
import ru.example.account.security.entity.enums.BlockedEntityType;

@Repository
public interface BannedEntityRepository extends JpaRepository<BannedEntity, Long> {

    @Query(value = """
                   SELECT EXISTS(SELECT TRUE FROM security.security.banned_entities AS sbe
                   WHERE sbe. entity_type = :blockedEntityType
                    AND sbe.banned_user_id = :userId)
                   """, nativeQuery = true)
    boolean isEntityBanned(@Param("blockedEntityType") BlockedEntityType blockedEntityType, @Param("userId") Long userId);


    // --- МЕТОД №2: Проверка по IP_ADDRESS ---
    @Query(value = """
                   SELECT EXISTS(
                       SELECT 1 FROM security.banned_entities
                       WHERE entity_type = 'IP_ADDRESS'
                       AND entity_value = :ipAddress 
                   )
                   """, nativeQuery = true)
    boolean isIpBanned(@Param("IP_ADDRESS") BlockedEntityType IP_ADDRESS, @Param("ipAddress") String ipAddress);


    // --- МЕТОД №3: Проверка по FINGERPRINT ---
    @Query(value = """
                   SELECT EXISTS(
                       SELECT 1 FROM security.banned_entities
                       WHERE entity_type = 'FINGERPRINT'
                       AND entity_value = :fingerprint
                   )
                   """, nativeQuery = true)
    boolean isFingerprintBanned(@Param("FINGERPRINT") BlockedEntityType FINGERPRINT, @Param("fingerprint") String fingerprint);
}
