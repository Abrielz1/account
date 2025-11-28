package ru.example.account.security.service.impl.facede;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.RevokedSessionArchive;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.repository.ActiveSessionCacheRepository;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedSessionArchiveRepository;
import ru.example.account.security.repository.SessionAuditLogRepository;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.facade.SessionRevocationServiceFacade;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class SessionRevocationServiceImpl implements SessionRevocationServiceFacade { // todo на вынос

    private final AuthSessionRepository authSessionRepository;

 //   private final BlacklistService blacklistService;

    private final RevokedSessionArchiveRepository revokedSessionArchiveRepository;

    private final SessionQueryService sessionQueryService;

    private final SessionAuditLogRepository auditLogRepository;

    private final ActiveSessionCacheRepository activeSessionCacheRepository;

    /**
     * Выполняет штатный, атомарный процесс отзыва и архивации ОДНОЙ сессии.
     * Реализует ТВОЮ гибкую логику с передачей статуса.
     */
    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public boolean revokeAndArchive(AuthSession sessionToRevoke, SessionStatus status, RevocationReason reason) {

        // 1. ПРОВЕРКИ ("СТРАЖНИК")
        if (sessionToRevoke == null) {
            log.warn("Attempt to revoke a null session.");
            return false;
        }

        Instant now = Instant.now();

        // 2. ОБНОВЛЕНИЕ АУДИТА
        this.auditLogRepository.findBySessionId(sessionToRevoke.getId()).ifPresent(auditLog -> {
            // Если причина или переданный статус - тревожные, помечаем аудит
            if (reason.equals(RevocationReason.REASON_ADMIN_ACTION) || this.isStatusSecurityAlert(status)) {
                auditLog.setCompromised(true);
            }
        });

        // 3. АРХИВАЦИЯ В POSTGRES
        this.revokedSessionArchiveRepository.save(RevokedSessionArchive.from(sessionToRevoke, now, reason, status));

        // 4. БЛЭКЛИСТИНГ ACCESS TOKEN'А и REFRESH TOKEN'А В REDIS
//        this.blacklistService.blacklistAccessToken(sessionToRevoke.getAccessToken());
//        this.blacklistService.blacklistRefreshToken(sessionToRevoke.getRefreshToken());

        // 5. ЗАЧИСТКА "ГОРЯЧИХ" ХРАНИЛИЩ
        this.activeSessionCacheRepository.deleteById(sessionToRevoke.getRefreshToken());
        this.authSessionRepository.delete(sessionToRevoke);

        log.info("Session {} successfully REVOKED and ARCHIVED. Reason: {}",
                sessionToRevoke.getId(), reason);
        return true;
    }

    /**
     * Выполняет экстренный отзыв ВСЕХ активных сессий пользователя.
     */
    @Override
    @Transactional(value = "securityTransactionManager")
    public CompletableFuture<Boolean> revokeAllSessionsForUser(Long userId, SessionStatus status, RevocationReason reason) {

        // Находим сессии по ПРАВИЛЬНОМУ статусу - ACTIVE.
        // Переданный `status` будем использовать для ЛОГИКИ, а не для поиска.
        List<AuthSession> activeSessions = sessionQueryService.getAllActiveSession(userId, SessionStatus.STATUS_ACTIVE);

        if (activeSessions.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        log.warn("Revoking all ({}) sessions for user {} with reason: {}",
                activeSessions.size(), userId, reason);

        // В цикле вызываем основной метод
        activeSessions.forEach(session -> {
            try {
                // Передаем и новый статус, и новую причину
                this.revokeAndArchive(session, status, reason);
            } catch (Exception e) {
                log.error("Failed to revoke single session {} during mass-revocation. Continuing...",
                        session.getId(), e);
            }
        });

        log.info("Finished mass revocation process for user {}", userId);
        return CompletableFuture.completedFuture(true);
    }

    private boolean isStatusSecurityAlert(SessionStatus status) {
        return status == SessionStatus.STATUS_COMPROMISED ||
                status == SessionStatus.STATUS_POTENTIAL_COMPROMISED ||
                status == SessionStatus.STATUS_RED_ALERT;
    }
}
