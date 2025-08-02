package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.service.BlacklistService;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private final StringRedisTemplate redisTemplate;

    private static final String BANNED_ACCESS_PREFIX = "banned_access::";
    private static final String BANNED_REFRESH_PREFIX = "banned_refresh::";


    @Override
    public void blacklistSessionTokens(AuthSession sessionToRevoke, Instant now) {

        this.blacklistAccessToken(sessionToRevoke.getAccessToken(), now);
        this.blacklistRefreshToken(sessionToRevoke.getRefreshToken(), now);
    }

    @Override
    public void blacklistAccessToken(String accessToken, Instant now) {

        log.info("");
        redisTemplate.opsForValue().set(BANNED_ACCESS_PREFIX + accessToken, "revoked",
                Duration.ofMinutes(15).toSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public void blacklistRefreshToken(String refreshToken, Instant now) {

        log.info("");
        redisTemplate.opsForValue().set(BANNED_REFRESH_PREFIX + refreshToken,
                "revoked", Duration.ofMinutes(15).toSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BANNED_ACCESS_PREFIX + accessToken));
    }

    @Override
    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BANNED_REFRESH_PREFIX + refreshToken));
    }
}

