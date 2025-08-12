package ru.example.account.security.service;

import org.springframework.security.core.GrantedAuthority;
import ru.example.account.security.entity.ActiveSessionCache;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления кешем АКТИВНЫХ сессий в Redis.
 * Предоставляет "умный", само-излечивающийся механизм
 * доступа к "горячим" данным сессии.
 */
public interface ActiveSessionCacheService {

    /**
     * "Умный" поиск: сначала ищет в Redis. Если нет - ищет в Postgres,
     * и если находит там - "прогревает" Redis кеш ("read-through/self-healing").
     * Это ОСНОВНОЙ метод для "читающих" операций.
     * @param sessionId ID сессии.
     * @return Optional, содержащий "горячие" данные сессии.
     */
    Optional<ActiveSessionCache> findAndVerifySession(final UUID sessionId,
                                                      final Long currentUserId,
                                                      final Collection<? extends GrantedAuthority> currentUserAuthorities);

    /**
     * Сохраняет "горячие" данные сессии в кеш Redis.
     * @param session POJO с данными для кеширования.
     */
    void saveSession(final ActiveSessionCache session);

    /**
     * Удаляет сессию из кеша Redis (например, при логауте).
     * @param sessionId ID сессии.
     */
    void deleteSessionById(final UUID sessionId);

}
