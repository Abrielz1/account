package ru.example.account.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.BlockedTarget;
import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface BlockedTargetRepository extends JpaRepository<BlockedTarget, Long> {

    @Query(value = """
                   SELECT EXISTS(SELECT TRUE FROM security.blocked_target AS bt
                    WHERE bt.target_type = CAST(:targetType AS text)
                       AND bt.target_value = :targetValue
                       AND bt.is_deleted = false)
                   """,nativeQuery = true)
    Boolean isTargetCurrentlyBlocked(@Param("targetType") String targetType, // Передаем ENUM как строку, так надежнее в нативе
                                     @Param("targetValue") String targetValue);

    /**
     * Ищет АКТИВНУЮ блокировку, используя CTE с ЯВНЫМ, JOIN-ом для
     * максимальной, читаемости и будущей, поддерживаемости.
     */
    @Query(value = """
        -- ШАГ 1: Готовим "шпаргалку" - ID-шники всех потенциально подходящих банов.
        WITH potential_blocks_cte AS (
            SELECT
                bt.id
            FROM 
                security.blocked_targets bt
            WHERE 
                bt.target_type = CAST(:targetType AS text)
                AND bt.target_value = :targetValue
                AND bt.is_deleted = false
        )
        -- ШАГ 2: Берем ОСНОВНУЮ таблицу...
        SELECT 
            bt.* 
        FROM 
            security.blocked_targets bt
        
        -- ШАГ 3,: ...и ЯВНО, СОЕДИНЯЕМ ее с нашей "шпаргалкой",
        -- чтобы отсечь все, что не прошло первый, фильтр.
        JOIN 
            potential_blocks_cte pbc ON bt.id = pbc.id

        -- ШАГ 4: И только ТЕПЕРЬ, на оставшихся, записях,
        -- применяем финальную, "тяжелую" проверку по времени.
        WHERE 
            (bt.expires_at IS NOT NULL AND bt.expires_at > :timeOfReleaseBan)
        LIMIT 1
        """, nativeQuery = true)
    Optional<BlockedTarget> findActiveBlock(
                                             @Param("targetType") String targetType,
                                             @Param("targetValue") String targetValue,
                                             @Param("timeOfReleaseBan") ZonedDateTime timeOfReleaseBan
    );
}
