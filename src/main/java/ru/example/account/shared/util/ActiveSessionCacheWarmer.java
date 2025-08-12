package ru.example.account.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RedisRepository;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveSessionCacheWarmer {

    private static final String CACHE_ERROR = "REDIS IS DOWN! ";

    private static final String CRITICAL_SECURITY_ALERT = "CRITICAL SECURITY ALERT: ";

    private static final String IMPOSTER_DETECTED = "IMPOSTER DETECTED!";

    // специальный параметризованный репозитория для Redis
    private final RedisRepository<String, ActiveSessionCache> redisRepository;

    // Наша ЕДИНАЯ, централизованная конфигурация
    private final RedisKeysProperties redisKeys;

    private final RedisKeyBuilderHelper redisKeyBuilderHelper;

    private final AuthSessionRepository authSessionRepository;
    /**
     * "Прогревает" кеш Redis на основе данных из "холодного" хранилища.
     *
     * @param dbSession     JPA-сущности AuthSession, "источник правды".
     * @param currentUserId id пользователя
     * @return Созданный и сохраненный POJO для кеша.
     */
    public ActiveSessionCache cacheWarmer(final AuthSession dbSession,
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

    /**
     * Сохраняет "горячие" данные сессии в кеш Redis.
     * @param session POJO с данными для кеширования.
     */
    public void saveSession(final ActiveSessionCache session) {

        if (session == null || session.getSessionId() == null) {
            throw new IllegalArgumentException("Session and Session ID cannot be null.");
        }

        try {
            redisRepository.save(
                    this.redisKeyBuilderHelper.buildKeyBySessionId(session.getSessionId()),
                    session,
                    redisKeys.getTtl().getActiveSession()
            );
        } catch (RedisConnectionFailureException e) {
            log.error(CACHE_ERROR + "Could not save cache for session {}.", session.getSessionId(), e);
            // НЕ пробрасываем exception, чтобы не сломать основной флоу
        }
    }

    public Optional<ActiveSessionCache> verifyOwnerAndReturn(final UUID dbSessionId,
                                                             final Long currentUserId,
                                                             final Collection<? extends GrantedAuthority> currentUserAuthorities) {

        final AuthSession authSessionFromDb = this.authSessionRepository.findBySessionIdAndUserIdAndStatusActive(
                dbSessionId, currentUserId
        ).orElse(new AuthSession());

        if (Objects.equals(authSessionFromDb, new AuthSession())) {
            log.error("No active session id db with such sessionId: {}", dbSessionId);
            return Optional.empty();
        }

        final ActiveSessionCache activeSessionCacheFromRedis = this.redisRepository.findByKey(
                this.redisKeyBuilderHelper.buildKeyBySessionId(dbSessionId)
        ).orElse(new ActiveSessionCache());

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

    public Optional<ActiveSessionCache> findInPostgresAndHealCache(final UUID sessionId,
                                                                   final Long currentUserId,
                                                                   final Collection<? extends GrantedAuthority> currentUserAuthorities) {
        return this.authSessionRepository.findById(sessionId)
                .filter(dbSession -> this.verifyOwnerAndReturn(dbSession.getId(), currentUserId, currentUserAuthorities).isPresent())
                .map(dbSession -> this.cacheWarmer(dbSession, currentUserId, currentUserAuthorities)); // <<<--- ВЫЗЫВАЕМ "ВАРМЕР"
    }
}
