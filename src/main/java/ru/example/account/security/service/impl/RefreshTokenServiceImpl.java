package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.RefreshToken;
import ru.example.account.security.repository.RefreshTokenRepository;
import ru.example.account.security.service.RefreshTokenService;
import ru.example.account.shared.exception.exceptions.RefreshTokenException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    public Optional<RefreshToken> getByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByTokenRefresh(refreshToken);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = RefreshToken
                .builder()
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration.toMillis()))
                .timeToLive(60L*5L)
                .build();

       return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken checkRefreshToken(RefreshToken refreshToken) {

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("Refresh token is expired! " + refreshToken.getToken()
                    + "Try reLogin!");
        } else {

            return refreshToken;
        }
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
