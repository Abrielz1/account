package ru.example.account.security.service.todelete;

import ru.example.account.security.entity.ActiveSessionCache;
import java.util.Optional;

public interface RefreshTokenService {

    Optional<ActiveSessionCache> getByRefreshToken(String refreshToken);

    ActiveSessionCache createRefreshToken(Long userId);

    ActiveSessionCache checkRefreshToken(ActiveSessionCache refreshToken);

    void deleteByUserId(Long userId);
}
