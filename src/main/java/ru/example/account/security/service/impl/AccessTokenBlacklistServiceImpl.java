package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.example.account.security.service.AccessTokenBlacklistService;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AccessTokenBlacklistServiceImpl implements AccessTokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    // Префикс для ключей, чтобы не смешивать их с другими данными в Redis.
    private static final String KEY_PREFIX = "banned_access_sessions::";


    @Override
    public void addToBlacklist(UUID sessionId, Duration duration) {
        // Не добавляем, если токен уже и так истек
        if (duration.isNegative() || duration.isZero()) {
            return;
        }

        // Ключ: "banned_access_sessions::uuid-...."
        // Значение: "revoked" (или любое другое, оно неважно)
        // TTL: оставшееся время жизни токена
        redisTemplate.opsForValue().set(
                KEY_PREFIX + sessionId.toString(),
                "revoked",
                duration.toSeconds(),
                TimeUnit.SECONDS
        );
    }

    @Override
    public boolean isBlacklisted(UUID sessionId) {
        // hasKey() - очень быстрая O(1) операция в Redis
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + sessionId.toString()));
    }
}
