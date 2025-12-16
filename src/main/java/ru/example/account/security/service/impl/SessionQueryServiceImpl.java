package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.enums.SessionStatus;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedSessionArchiveRepository;
import ru.example.account.security.service.worker.FingerprintService;
import ru.example.account.security.service.SessionQueryService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionQueryServiceImpl implements SessionQueryService {

    private final AuthSessionRepository authSessionRepository;

    private final FingerprintService fingerprintService;

    private final RevokedSessionArchiveRepository revokedTokenArchiveRepository;

    @Override
    public Optional<AuthSession> findById(UUID sessionId) {
        return this.authSessionRepository.findById(sessionId);
    }

    @Override
    @Transactional(value = "securityTransactionManager")
    public Boolean checkExistenceOfFingerprint(String fingerprintHash) {
        log.info("Cheking exsitense of fingerprintHash");
        return this.fingerprintService.isFingerPrintAreKnown(fingerprintHash);
    }

    @Override
    public Optional<AuthSession> findByRefreshTokenAndStatus(String refreshToken) {
        return this.authSessionRepository.findByRefreshTokenAndStatus(refreshToken, SessionStatus.STATUS_ACTIVE);
    }

    @Override
    public Optional<AuthSession> findActiveByAccessToken(String accessToken) {

        return this.authSessionRepository.findByAccessTokenAndStatus(accessToken, SessionStatus.STATUS_ACTIVE);
    }

    @Override
    public boolean isTokenArchived(String refreshToken) {
        return this.revokedTokenArchiveRepository.checkRefreshTokenInRevokedArchive(refreshToken);
    }

    @Override
    public List<AuthSession> getAllActiveSession(Long userId, SessionStatus sessionStatus) {
        return this.authSessionRepository.findAllByUserIdAndStatus(userId, sessionStatus);
    }

    @Override
    public Optional<String> getFingerPrint(String refreshToken) {
        return this.authSessionRepository.findOriginalFingerprintByActiveRefreshToken(refreshToken);
    }
}
