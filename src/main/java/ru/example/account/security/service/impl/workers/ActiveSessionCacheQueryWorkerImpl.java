package ru.example.account.security.service.impl.workers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.dto.SessionVerificationResult;
import ru.example.account.security.dto.VerificationStatus;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.service.ActiveSessionCacheQueryWorker;
import ru.example.account.shared.util.ActiveSessionCacheWarmer;
import ru.example.account.shared.util.RedisKeyBuilderHelper;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveSessionCacheQueryWorkerImpl implements ActiveSessionCacheQueryWorker {

    // специальный параметризованный репозитория для Redis
    private final RedisRepository<String, ActiveSessionCache> redisRepository;

    private final ActiveSessionCacheWarmer activeSessionCacheWarmer;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;

    /**
     * "Умный" поиск: сначала ищет в Redis. Если нет - ищет в Postgres,
     * и если находит там - "прогревает" Redis кеш ("read-through/self-healing").
     * Это ОСНОВНОЙ метод для "читающих" операций.
     * @param sessionId ID сессии.
     * @return Optional, содержащий "горячие" данные сессии.
     */
    @Transactional(value = "businessTransactionManager", readOnly = true)
    public SessionVerificationResult findAndVerifySession(final UUID sessionId,
                                                             final Long currentUserId,
                                                             final Collection<? extends GrantedAuthority> currentUserAuthorities) {

        if (sessionId == null && currentUserId == null && currentUserAuthorities == null) {
            log.error("No valid data was given!");
            return new SessionVerificationResult(VerificationStatus.SESSION_NOT_FOUND, Optional.empty());
        }

        if (currentUserAuthorities.isEmpty()) {
            log.error("No valid roles was given!");
            return new SessionVerificationResult(VerificationStatus.SESSION_NOT_FOUND, Optional.empty());
        }
        Optional<ActiveSessionCache> activeSessionCacheFromRedis = Optional.empty();
        // --- ЭШЕЛОН 1: REDIS ("Горячий" кеш) ---
        try {

            if (sessionId != null) {
                activeSessionCacheFromRedis = this.redisRepository.findByKey(
                    this.redisKeyBuilderHelper.buildKeyBySessionId(sessionId)
                );
            }

            if (activeSessionCacheFromRedis.isPresent()
                    && Objects.equals(currentUserId, activeSessionCacheFromRedis.get().getUserId())) {
                log.info("session s valid!");
                return new SessionVerificationResult(VerificationStatus.CACHE_DATA_STALE,
                        this.activeSessionCacheWarmer.verifyOwnerAndReturn(sessionId, currentUserId, currentUserAuthorities));
            }

            log.error("CRITICAL SECURITY ALERT: IMPOSTER DETECTED IN CACHE! User {} accessing session of session with {}",
                    currentUserId, sessionId);

            return new SessionVerificationResult(VerificationStatus.IMPOSTER_DETECTED, Optional.empty());
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN!", e);
        }
        // --- ЭШЕЛОН 2: POSTGRES + "САМО-ИЗЛЕЧЕНИЕ" ---
        log.warn("Cache miss for session {}. Checking Postgres.", sessionId);



        return new SessionVerificationResult(VerificationStatus.VALID,
                this.activeSessionCacheWarmer.findInPostgresAndHealCache(sessionId, currentUserId, currentUserAuthorities));
    }
}
