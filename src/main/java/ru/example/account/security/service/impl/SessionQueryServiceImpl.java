package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.service.FingerprintService;
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

    private final RevokedTokenArchiveRepository revokedTokenArchiveRepository;

    @Override
    public Optional<AuthSession> findById(UUID sessionId) {
        return this.authSessionRepository.findById(sessionId);
    }

    @Override
    @Transactional(value = "securityTransactionManager")
    public Boolean checkExistenceOfFingerprint(String fingerprint) {
        log.info("Cheking exsitense of fingerprintHash");
        return fingerprintService.isFingerPrintAreKnown(fingerprint);
    }

    @Override
    public AuthSession findByRefreshToken(String refreshToken) {
        return this.authSessionRepository.findByRefreshTokenAndStatus(refreshToken, SessionStatus.STATUS_ACTIVE).orElseThrow(() -> {
            log.error("Hacker ALERT!");
            return new IllegalStateException("Our Dns fails");
        });
    }

    @Override
    public Optional<AuthSession> findActiveByRefreshToken(String refreshToken) {

        return authSessionRepository.findByRefreshTokenAndStatus(refreshToken, SessionStatus.STATUS_ACTIVE);
    }

    @Override
    public boolean isTokenArchived(String refreshToken) {
        return revokedTokenArchiveRepository.checkRefreshTokenInRevokedArchive(refreshToken);
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
