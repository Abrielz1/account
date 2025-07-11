package ru.example.account.security.service.temp;

import ru.example.account.security.entity.RefreshToken;
import java.util.Optional;

public interface RefreshTokenService {

    Optional<RefreshToken> getByRefreshToken(String refreshToken);

    RefreshToken createRefreshToken(Long userId);

    RefreshToken checkRefreshToken(RefreshToken refreshToken);

    void deleteByUserId(Long userId);
}
