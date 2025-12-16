package ru.example.account.security.service.impl.facede;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.BlackListedRefreshToken;
import ru.example.account.security.entity.BlacklistedAccessToken;
import ru.example.account.security.entity.enums.RevocationReason;
import ru.example.account.security.entity.RevokedSessionArchive;
import ru.example.account.security.entity.enums.SessionStatus;
import ru.example.account.security.repository.ActiveSessionCacheRepository;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.BlacklistedAccessTokenRepository;
import ru.example.account.security.repository.BlacklistedRefreshTokenRepository;
import ru.example.account.security.repository.RevokedSessionArchiveRepository;
import ru.example.account.security.repository.SessionAuditLogRepository;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.facade.SessionRevocationServiceFacade;
import ru.example.account.security.service.worker.BlacklistAccessTokenCommandWorker;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionRevocationServiceImplV2 implements SessionRevocationServiceFacade { // todo снести

    private final AuthSessionRepository authSessionRepository;

    private final RevokedSessionArchiveRepository revokedSessionArchiveRepository;

    private final SessionQueryService sessionQueryService;

    private final SessionAuditLogRepository auditLogRepository;

    private final ActiveSessionCacheRepository activeSessionCacheRepository;

    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;

    private final BlacklistedRefreshTokenRepository blacklistedRefreshTokenRepository;

    private final BlacklistAccessTokenCommandWorker blacklistCommandWorker;

    @Override
    public boolean revokeAndArchive(AuthSession sessionToRevoke, SessionStatus status, RevocationReason reason) {

    // 1. ПРОВЕРКИ
    if (sessionToRevoke == null) { // NB if session is Null, so no session to revoke its bug!
        log.warn("Attempt to revoke a null session.");
        return false;
    }

        Instant now = Instant.now(); // NB We make a ONE per revoke time stamp. throughout whole method

        this.auditLogRepository.findBySessionId(sessionToRevoke.getId()).ifPresent(auditLog -> { // NB its stub for admin revocation or our future pro active session analyzer
            // Если причина или переданный статус - тревожные, помечаем аудит

            if (reason.equals(RevocationReason.REASON_ADMIN_ACTION) || this.isStatusSecurityAlert(status)) {
                log.info("Session with sessionId: {} was setted as compromides!", sessionToRevoke.getId());
                auditLog.setCompromised(true);
                // todo создать в перспективе уведомление ля проверки подозрительного поведения!
            }
        });

        BlacklistedAccessToken blacklistedAccessToken = new BlacklistedAccessToken();
        BlackListedRefreshToken blackLictedRefreshToken = new BlackListedRefreshToken();

        // 3. АРХИВАЦИЯ СЕССИИ и ОБОИХ ТОКЕНОВ В POSTGRES

        RevokedSessionArchive revokedSessionArchive = new RevokedSessionArchive();
        revokedSessionArchive.setUp(sessionToRevoke, reason, status);

        this.revokedSessionArchiveRepository.save(revokedSessionArchive);
        this.blacklistedRefreshTokenRepository.save(blackLictedRefreshToken.setUp(sessionToRevoke, now, reason));
        this.blacklistedAccessTokenRepository.save(blacklistedAccessToken.setUp(sessionToRevoke, now, reason));

        // 4. БЛЭКЛИСТИНГ ACCESS TOKEN'А и REFRESH TOKEN'А В REDIS
        //  this.blacklistCommandWorker.blacklistRefreshToken(sessionToRevoke.getRefreshToken());
     //   this.blacklistCommandWorker.blacklistAccessToken(sessionToRevoke.getAccessToken(), r);

        // 5. ЗАЧИСТКА ХРАНИЛИЩ
        this.activeSessionCacheRepository.deleteById(sessionToRevoke.getRefreshToken());
        this.authSessionRepository.delete(sessionToRevoke);

        log.info("Session {} successfully REVOKED and ARCHIVED. Reason: {}",
                sessionToRevoke.getId(), reason);
        return true;
    }

    @Override
    public CompletableFuture<Boolean> revokeAllSessionsForUser(Long userId, SessionStatus status, RevocationReason reason) {
        // Находим сессии по статусу - ACTIVE.
        // Переданный `status` будем использовать для ЛОГИКИ.
        List<AuthSession> activeSessions = sessionQueryService.getAllActiveSession(userId, SessionStatus.STATUS_ACTIVE);

        if (activeSessions.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        log.warn("Revoking all ({}) sessions for user {} with reason: {}",
                activeSessions.size(), userId, reason);

        // В цикле вызываем наш основной метод
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
