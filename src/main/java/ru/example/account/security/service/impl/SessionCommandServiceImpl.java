package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.ActiveSessionCacheRepository;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.service.IdGenerationService;
import ru.example.account.security.service.SessionCommandService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCommandServiceImpl implements SessionCommandService {

    private final AuthSessionRepository authSessionRepository;

    private final RevokedTokenArchiveRepository archiveRepository;

    private final ActiveSessionCacheRepository activeSessionCacheRepository;

    private final RedissonClient redissonClient;

    private final JwtUtils jwtUtils;

    private final IdGenerationService idGenerationService;

    @Override
    public void archive(AuthSession session, RevocationReason reason) {

    }

    @Override
    public void archiveAllForUser(Long userId, RevocationReason reason) {

    }
}
