package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.BlackListedRefreshToken;
import ru.example.account.security.entity.BlacklistedAccessToken;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.repository.BlacklistedAccessTokenRepository;
import ru.example.account.security.repository.BlacklistedRefreshTokenRepository;
import ru.example.account.security.service.worker.BlacklistAccessTokenCommandWorker;
import ru.example.account.security.service.worker.BlacklistRefreshTokenCommandWorker;
import ru.example.account.security.service.worker.SessionCacheCleanupWorker;
import ru.example.account.security.service.worker.WhitelistAccessTokenCommandWorker;
import ru.example.account.security.service.worker.WhitelistRefreshTokenCommandWorker;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCacheCleanupWorkerImpl implements SessionCacheCleanupWorker {

    private final BlacklistedRefreshTokenRepository blacklistedRefreshTokenRepository;

    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;

    private final BlacklistAccessTokenCommandWorker blacklistAccessTokenCommandWorker;

    private final BlacklistRefreshTokenCommandWorker blacklistRefreshTokenCommandWorker;

    private final WhitelistAccessTokenCommandWorker whitelistAccessTokenCommandWorker;

    private final WhitelistRefreshTokenCommandWorker whitelistRefreshTokenCommandWorker;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void cleanup(AuthSession sessionToRevoke, RevocationReason revocationReason) {

        this.whitelistAccessTokenCommandWorker.deWhiteListAccessToken(sessionToRevoke.getAccessToken());
        this.whitelistRefreshTokenCommandWorker.deWhiteListRefreshToken(sessionToRevoke.getRefreshToken());
        this.blacklistRefreshTokenCommandWorker.blacklistRefreshToken(sessionToRevoke.getRefreshToken());
        this.blacklistAccessTokenCommandWorker.blacklistAccessToken(sessionToRevoke, revocationReason);

        BlacklistedAccessToken blacklistedAccessToken = new BlacklistedAccessToken();
        BlackListedRefreshToken blackLictedRefreshToken = new BlackListedRefreshToken();

        blacklistedAccessToken.setUp(sessionToRevoke, Instant.now(), revocationReason);
        blackLictedRefreshToken.setUp(sessionToRevoke, Instant.now(), revocationReason);

        this.blacklistedAccessTokenRepository.save(blacklistedAccessToken);
        this.blacklistedRefreshTokenRepository.save(blackLictedRefreshToken);
    }
}
