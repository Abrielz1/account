package ru.example.account.security.service.temp;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RefreshToken;

public interface UniqueIdGeneratorService {

    RefreshToken createRefreshToken(Long userId, String fingerprintHash, String ipAddress, String userAgent);

    AuthSession createAuthSession(Long userId, String fingerprintHash, String ipAddress, String userAgent);
}
