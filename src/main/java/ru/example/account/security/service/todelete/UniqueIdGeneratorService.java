package ru.example.account.security.service.todelete;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.ActiveSessionCache;

public interface UniqueIdGeneratorService {

    ActiveSessionCache createRefreshToken(Long userId, String fingerprintHash, String ipAddress, String userAgent);

    AuthSession createAuthSession(Long userId, String fingerprintHash, String ipAddress, String userAgent);
}
