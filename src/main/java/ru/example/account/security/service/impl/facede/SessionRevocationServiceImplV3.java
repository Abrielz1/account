package ru.example.account.security.service.impl.facede;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.service.facade.SessionRevocationServiceFacade;
import ru.example.account.security.service.orcestrator.SessionRevocationOrchestrator;

import java.util.concurrent.CompletableFuture;
// todo написать норм документацию!
/**
 * Это фасад, он самый главный в манипуляции сессиями, сам ничего не делае, но скидывает работу ниже
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionRevocationServiceImplV3 implements SessionRevocationServiceFacade {

    private final SessionRevocationOrchestrator sessionRevocationOrchestrator;

    @Override
    public boolean revokeAndArchive(AuthSession sessionToRevoke, SessionStatus status, RevocationReason reason) {
        log.info("[INFO]: Received command to revoke SINGLE session {}", sessionToRevoke.getId());
        return this.sessionRevocationOrchestrator.orchestrateSingleRevocation(sessionToRevoke, status, reason);
    }

    @Override
    public CompletableFuture<Boolean> revokeAllSessionsForUser(Long userId, SessionStatus status, RevocationReason reason) {



        log.warn("[INFO]: Received command to revoke ALL sessions for user {}", userId);
        return this.sessionRevocationOrchestrator.orchestrateMassRevocation(userId, status, reason);
    }
}
