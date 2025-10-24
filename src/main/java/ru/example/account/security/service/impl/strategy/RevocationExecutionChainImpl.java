package ru.example.account.security.service.impl.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.service.RevocationStrategy;
import ru.example.account.security.service.strategy.RevocationExecutionChain;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevocationExecutionChainImpl implements RevocationExecutionChain {

    private final AuthSessionRepository authSessionRepository;

    private final RevocationStrategy sessionCommandService;

    @Override
    public boolean execute(AuthSession session, SessionStatus status, RevocationReason reason) {

        return this.sessionCommandService.archiveAllForUser(session.getUserId(), session.getFingerprint(), session.getIpAddress(), session.getUserAgent(), reason);
    }

    @Override
    public CompletableFuture<Boolean> execute(Long userId, SessionStatus status, RevocationReason reason) {

        List<Boolean> proceededSessionsToRevoke = new LinkedList<>();
        List<AuthSession> authSessionList = this.authSessionRepository.findAllByUserIdAndStatus(userId, status);

        if (authSessionList.isEmpty()) {
            log.warn("[WARN] active sessions to mass revoke is not fond");
            return CompletableFuture.completedFuture(true);
        }

        for (AuthSession iter : authSessionList) {
            try {

                log.info("[INFO] session {} is going to revoke!", iter);
                this.sessionCommandService.archiveAllForUser(userId, iter.getFingerprint(), iter.getIpAddress(), iter.getUserAgent(), reason);
                proceededSessionsToRevoke.add(true);
            } catch (Exception e) {
                log.error("[ERROR] something due mass revocation throw exception!");
                proceededSessionsToRevoke.add(false);
            }
        }
        // todo потом в мапу сессия и статус и в ПОЧТУ!
        return CompletableFuture.completedFuture(!proceededSessionsToRevoke.contains(false));
    }
}
