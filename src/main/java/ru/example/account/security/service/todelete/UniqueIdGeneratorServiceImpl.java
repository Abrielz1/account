package ru.example.account.security.service.todelete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniqueIdGeneratorServiceImpl  implements UniqueIdGeneratorService {

    private final AuthSessionRepository authSessionRepository;

    private final RevokedTokenArchiveRepository archiveRepository;

    private final RedissonClient redissonClient;

    private static final String SESSION_ID_LOCK_KEY = "lock:generate:session-id";

@Override
@Transactional
public ActiveSessionCache createRefreshToken(Long userId, String fingerprintHash, String ipAddress, String userAgent) {

    return null;
}
    @Override
    @Transactional
    public AuthSession createAuthSession(Long userId, String fingerprintHash, String ipAddress, String userAgent) {

        return null;
    }
}
