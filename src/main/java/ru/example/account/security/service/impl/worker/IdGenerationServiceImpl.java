package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.repository.SessionAuditLogRepository;
import ru.example.account.security.service.worker.IdGenerationService;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdGenerationServiceImpl implements IdGenerationService {


    private final AuthSessionRepository authSessionPostgresRepo; // Для Postgres

    private final RevokedTokenArchiveRepository revokedTokenArchiveRepository;

    private final SessionAuditLogRepository sessionAuditLogRepository; // Для Postgres

    private final RedissonClient redissonClient;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int MAX_ATTEMPTS = 5;

    @Override
    public UUID generateSessionId() {

        final RLock lock = redissonClient.getLock("lock:gen:session-id");

        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Cannot acquire lock for sessionId");
            }

            byte[] sessionBytes = new byte[16];
            secureRandom.nextBytes(sessionBytes);

            UUID sessionId = UUID.nameUUIDFromBytes(sessionBytes);

            final int MAX_VALUE = 4;

            int CURRENT_VALUE = 0;

            while (CURRENT_VALUE < MAX_VALUE) {

                if (authSessionPostgresRepo.checkSessionIdAuditLog(sessionId.toString())
                        && sessionAuditLogRepository.checkSessionIdAuthSession(sessionId.toString())) {

                    sessionId = UUID.nameUUIDFromBytes(sessionBytes);;
                    ++CURRENT_VALUE;
                } else {

                    break;
                }

                if (CURRENT_VALUE >= MAX_VALUE) {
                    log.error("CRITICAL: Failed to generate a unique session ID after {} attempts.", MAX_VALUE);
                    throw new IllegalStateException("Cannot generate a unique session ID.");
                }
            }

            return sessionId;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Session ID generation interrupted", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public String generateUniqueTokenId() {
        final RLock lock = redissonClient.getLock("lock:gen:refresh-token");
        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Failed to acquire lock for refresh token generation");
            }

            byte[] sessionBytes = new byte[16];
            secureRandom.nextBytes(sessionBytes);

            String token = UUID.nameUUIDFromBytes(sessionBytes).toString();

            int currentAttempt = 0;
            while (currentAttempt < MAX_ATTEMPTS) {
                // Простая, явная проверка в ОБЕИХ таблицах
                if (Boolean.TRUE.equals(!authSessionPostgresRepo.existsByRefreshToken(token))
                        && Boolean.TRUE.equals(!revokedTokenArchiveRepository.existsById(token))) {
                    return token;
                }

                token = UUID.nameUUIDFromBytes(sessionBytes).toString();

                ++currentAttempt;
            }
            throw new IllegalStateException("Failed to generate unique refresh token");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("RefreshToken generation interrupted", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
