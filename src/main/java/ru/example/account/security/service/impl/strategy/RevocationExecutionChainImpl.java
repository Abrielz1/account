package ru.example.account.security.service.impl.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.service.SessionCommandService;
import ru.example.account.security.service.strategy.RevocationExecutionChain;
import ru.example.account.security.service.worker.ActiveSessionCacheCommandWorker;
import ru.example.account.security.service.worker.ActiveSessionCacheQueryWorker;
import ru.example.account.security.service.worker.BlacklistCommandWorker;
import ru.example.account.security.service.worker.WhitelistCommandWorker;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevocationExecutionChainImpl implements RevocationExecutionChain {

    private final BlacklistCommandWorker blacklistCommandWorker;

    private final WhitelistCommandWorker whitelistCommandWorker;

    private final AuthSessionRepository authSessionRepository;

    private final ActiveSessionCacheQueryWorker activeSessionCacheQueryWorker;

    private final ActiveSessionCacheCommandWorker activeSessionCacheCommandWorker;

    private final SessionCommandService sessionCommandService;

    @Override
    public boolean execute(AuthSession session, SessionStatus status, RevocationReason reason) {

        return false;
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
             //   this.revocationExecutionChain.execute(iter, status, reason);
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
