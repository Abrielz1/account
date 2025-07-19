package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.service.SessionQueryService;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionQueryServiceImpl implements SessionQueryService {

    private final AuthSessionRepository authSessionRepository;

    @Override
    public Optional<AuthSession> findById(UUID sessionId) {
        return Optional.empty();
    }

    @Override
    @Transactional(value = "securityTransactionManager")
    public Boolean checkExistenceOfFingerprint(String fingerprintHash) {
        log.info("Cheking exsitense of fingerprintHash");
        return authSessionRepository.existsByFingerprintHash(fingerprintHash);
    }

    @Override
    public AuthSession findByRefreshToken(String refreshToken) {
        return this.authSessionRepository.findByRefreshTokenAndStatus(refreshToken, SessionStatus.STATUS_ACTIVE).orElseThrow(() -> {
            log.error("Hacker ALERT!");
            return new IllegalStateException(""); // todo
        });
    }

    @Override
    public Optional<AuthSession> findActiveByRefreshToken(String token) {



        return Optional.empty();
    }
}
