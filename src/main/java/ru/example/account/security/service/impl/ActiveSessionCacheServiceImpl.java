package ru.example.account.security.service.impl;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.service.ActiveSessionCacheService;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveSessionCacheServiceImpl implements ActiveSessionCacheService {

    private static final String CACHE_ERROR = "REDIS IS DOWN! ";

    private static final String CRITICAL_SECURITY_ALERT = "CRITICAL SECURITY ALERT: ";

    private static final String IMPOSTER_DETECTED = "IMPOSTER DETECTED!";

    // специальный параметризованный репозитория для Redis
    private final RedisRepository<String, ActiveSessionCache> redisRepository;

    // "Источник Правды" для сверки и "прогрева"
    private final AuthSessionRepository authSessionRepository;

    // Наша ЕДИНАЯ, централизованная конфигурация
    private final RedisKeysProperties redisKeys;

    @Transactional(value = "businessTransactionManager", readOnly = true)
    public Optional<ActiveSessionCache> findAndVerifySession(final UUID sessionId,
                                                             final Long currentUserId,
                                                             final Collection<? extends GrantedAuthority> currentUserAuthorities) {

        if (sessionId == null && currentUserId == null  && currentUserAuthorities == null) {
            log.error("No valid data was given!");
            return Optional.empty();
        }

        if (currentUserAuthorities.isEmpty()) {
            log.error("No valid roles was given!");
            return Optional.empty();
        }

        // --- ЭШЕЛОН 1: REDIS ("Горячий" кеш) ---
        try {

            Optional<ActiveSessionCache> activeSessionCacheFromRedis = this.redisRepository.findByKey(this.buildKey(sessionId));

            if (activeSessionCacheFromRedis.isPresent()
                    && Objects.equals(currentUserId, activeSessionCacheFromRedis.get().getUserId())) {
                log.info("session s valid!");
                return this.verifyOwnerAndReturn(sessionId, currentUserId, currentUserAuthorities);
            }

            log.error("CRITICAL SECURITY ALERT: IMPOSTER DETECTED IN CACHE! User {} accessing session of session with {}", currentUserId, sessionId);
            return Optional.empty();
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN!", e);
        }
        // --- ЭШЕЛОН 2: POSTGRES + "САМО-ИЗЛЕЧЕНИЕ" ---
        log.warn("Cache miss for session {}. Checking Postgres.", sessionId);

        return this.findInPostgresAndHealCache(sessionId, currentUserId, currentUserAuthorities);
    }

    @Override
    public void saveSession(final ActiveSessionCache session) {

        if (session == null || session.getSessionId() == null) {
            throw new IllegalArgumentException("Session and Session ID cannot be null.");
        }

        try {
            redisRepository.save(
                    buildKey(session.getSessionId()),
                    session,
                    redisKeys.getTtl().getActiveSession()
            );
        } catch (RedisConnectionFailureException e) {
            log.error(CACHE_ERROR + "Could not save cache for session {}.", session.getSessionId(), e);
            // НЕ пробрасываем exception, чтобы не сломать основной флоу
        }
    }

    @Override
    public void deleteSessionById(final UUID sessionId) {

        if (sessionId == null) {
            log.error("no session with sessionId is null to delete!");
            return;
        }

        if (redisRepository.exists(this.buildKey(sessionId))) {

            try {
                redisRepository.delete(buildKey(sessionId));
            } catch (RedisConnectionFailureException e) {
                log.error(CACHE_ERROR + "Could not delete session {}.", sessionId, e);
            }
        } else {
            log.error("no session with sessionId: {} to delete!", sessionId);
        }
    }

    private Optional<ActiveSessionCache> verifyOwnerAndReturn(final UUID dbSessionId,
                                                              final Long currentUserId,
                                                              final Collection<? extends GrantedAuthority> currentUserAuthorities) {

        final AuthSession authSessionFromDb = this.authSessionRepository.findBySessionIdAndUserIdAndStatusActive(
                dbSessionId, currentUserId
        ).orElse(new AuthSession());

        if (Objects.equals(authSessionFromDb, new AuthSession())) {
            log.error("No active session id db with such sessionId: {}", dbSessionId);
            return Optional.empty();
        }

        final ActiveSessionCache activeSessionCacheFromRedis = this.redisRepository.findByKey(this.buildKey(dbSessionId))
                .orElse(new ActiveSessionCache());

        if (Objects.equals(activeSessionCacheFromRedis, new ActiveSessionCache())) {
            log.error(CRITICAL_SECURITY_ALERT + IMPOSTER_DETECTED + " IN CACHE! User {} accessing session of session with {}.",
                    currentUserId, dbSessionId);
            return Optional.empty();
        }

        if (!Objects.equals(authSessionFromDb.getFingerprintHash(), activeSessionCacheFromRedis.getFingerprintHash())) {
            log.error(CRITICAL_SECURITY_ALERT + IMPOSTER_DETECTED);
            return Optional.empty();
        }

        if (!Objects.equals(authSessionFromDb.getAccessToken(), activeSessionCacheFromRedis.getAccessToken())) {
            log.error(CRITICAL_SECURITY_ALERT + IMPOSTER_DETECTED);
            return Optional.empty();
        }

        if (!Objects.equals(authSessionFromDb.getRefreshToken(), activeSessionCacheFromRedis.getRefreshToken())) {
            log.error(CRITICAL_SECURITY_ALERT + IMPOSTER_DETECTED);
            return Optional.empty();
        }

        return Optional.of(this.cacheWarmer(authSessionFromDb, currentUserId, currentUserAuthorities));
    }


    private Optional<ActiveSessionCache> findInPostgresAndHealCache(final UUID sessionId,
                                                                    final Long currentUserId,
                                                                    final Collection<? extends GrantedAuthority> currentUserAuthorities) {
        return this.authSessionRepository.findById(sessionId)
                .filter(dbSession -> this.verifyOwnerAndReturn(dbSession.getId(), currentUserId, currentUserAuthorities).isPresent())
                .map(dbSession -> this.cacheWarmer(dbSession, currentUserId, currentUserAuthorities)); // <<<--- ВЫЗЫВАЕМ "ВАРМЕР"
    }

    // "Тупой" собиральщик ключа из префикса (из YAML) и ID
    private String buildKey(UUID sessionId) {
        return this.redisKeys.getKeys().getActiveSessionPrefix() + sessionId.toString();
    }

    /**
     * "Прогревает" кеш Redis на основе данных из "холодного" хранилища.
     *
     * @param dbSession     JPA-сущности AuthSession, "источник правды".
     * @param currentUserId id пользователя
     * @return Созданный и сохраненный POJO для кеша.
     */
    private ActiveSessionCache cacheWarmer(final AuthSession dbSession,
                                           final Long currentUserId,
                                           final Collection<? extends GrantedAuthority> currentUserAuthorities) {
        log.info("Warming up cache for valid user {} and session {}.", currentUserId, dbSession.getId());

        if (!Objects.equals(dbSession.getUserId(), currentUserId)) {
            log.error("");
            return new ActiveSessionCache();
        }

        final ActiveSessionCache newActiveSessionCache = this.mapToCachePojo(dbSession, currentUserId, currentUserAuthorities);

        try {
            this.saveSession(newActiveSessionCache);
            log.info("Cache for session {} was warmed up successfully.", dbSession.getId());
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS DOWN! Cache warming process failed for session {} due to an unexpected error during save operation.",
                    dbSession.getId(), e);
        }

        return newActiveSessionCache;
    }

    private ActiveSessionCache mapToCachePojo(final AuthSession dbSession, final Long currentUserId, Collection<? extends GrantedAuthority> currentUserAuthorities) {
        return ActiveSessionCache.builder()
                .sessionId(dbSession.getId())
                .userId(currentUserId)
                .fingerprintHash(dbSession.getFingerprintHash())
                .roles(currentUserAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .expiresAt(dbSession.getExpiresAt())
                .ttl(redisKeys.getTtl().getActiveSession())
                .build();
    }
}
