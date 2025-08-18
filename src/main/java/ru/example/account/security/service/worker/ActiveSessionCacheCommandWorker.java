package ru.example.account.security.service.worker;

import ru.example.account.security.entity.ActiveSessionCache;
import java.util.UUID;

/**
 * COMMAND-ВОРКЕР.
 * Умеет ТОЛЬКО, СОЗДАВАТЬ и УДАЛЯТЬ записи в кеше сессий.
 */
public interface ActiveSessionCacheCommandWorker { /**
 * Сохраняет объект сессии в Redis с правильным TTL.
 */
void saveSession(ActiveSessionCache session);

    /**
     * Удаляет сессию из Redis по ее ID.
     */
    void deleteSessionById(UUID sessionId);
}
