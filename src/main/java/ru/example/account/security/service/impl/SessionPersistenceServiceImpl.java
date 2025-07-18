package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.service.SessionPersistenceService;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionPersistenceServiceImpl implements SessionPersistenceService {

    @Override
    public AuthSession createAndSaveSession(Long userId, UUID sessionId, String refreshToken) {
        return null;
    }

    @Override
    public ActiveSessionCache createAndSaveRedisToken(AuthSession session) {
        return null;
    }

    @Override
    public void createAndSaveAuditLog(AuthSession session) {

    }
}
