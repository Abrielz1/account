package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.service.SessionService;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final AuthSessionRepository authSessionRepository;

    private final RevokedTokenArchiveRepository archiveRepository;

    @Override
    public AuthSession createNew(Long userId, String fingerprint, String ip, String userAgent) {
        return null;
    }

    @Override
    public Optional<AuthSession> findActiveByRefreshToken(String token) {
        return Optional.empty();
    }

    @Override
    public Optional<AuthSession> findById(UUID sessionId) {
        return Optional.empty();
    }

    @Override
    public void archive(AuthSession session, RevocationReason reason) {

    }

    @Override
    public void archiveAllForUser(Long userId, RevocationReason reason) {

    }
}
