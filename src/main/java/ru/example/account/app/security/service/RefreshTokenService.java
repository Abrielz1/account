package ru.example.account.app.security.service;

import ru.example.account.app.entity.RefreshToken;
import java.util.Optional;

public interface RefreshTokenService {

    Optional<RefreshToken> getByRefreshToken(String refreshToken);

    RefreshToken createRefreshToken(Long userId);

    RefreshToken checkRefreshToken(RefreshToken refreshToken);

    void deleteByUserId(Long userId);
}
