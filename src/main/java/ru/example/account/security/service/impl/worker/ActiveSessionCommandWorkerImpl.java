package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.service.worker.ActiveSessionCacheCommandWorker;
import ru.example.account.shared.util.ActiveSessionCacheWarmer;
import ru.example.account.shared.util.RedisKeyBuilderHelper;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveSessionCommandWorkerImpl implements ActiveSessionCacheCommandWorker {
    private static final String CACHE_ERROR = "REDIS IS DOWN! ";


    // специальный параметризованный репозитория для Redis
    private final RedisRepository<String, ActiveSessionCache> redisRepository;

    private final ActiveSessionCacheWarmer activeSessionCacheWarmer;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;


    @Override
    public void saveSession(ActiveSessionCache session) {

        if (session == null) {
            log.error("");
            return;
        }

        this.activeSessionCacheWarmer.saveSession(session);
    }

    /**
     * Удаляет сессию из кеша Redis (например, при логауте).
     * @param sessionId ID сессии.
     */
    @Override
    public void deleteSessionById(UUID sessionId) {

        if (sessionId == null) {
            log.error("no session with sessionId is null to delete!");
            return;
        }

        if (redisRepository.exists(this.redisKeyBuilderHelper.buildKeyBySessionId(sessionId))) {

            try {
                redisRepository.delete(this.redisKeyBuilderHelper.buildKeyBySessionId(sessionId));
            } catch (RedisConnectionFailureException e) {
                log.error(CACHE_ERROR + "Could not delete session {}.", sessionId, e);
            }
        } else {
            log.error("no session with sessionId: {} to delete!", sessionId);
        }
    }
}
