package ru.example.account.security.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.io.Serial;
import java.util.concurrent.TimeUnit;

@Getter
@RedisHash("refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ActiveSessionCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    // --- КЛЮЧ: HMAC-ХЭШ ФИНГЕРПРИНТА! ---
    private String fingerprintHash;
    /**

     ID сессии (из AuthSession).

     Это НЕ ключ в Redis, ключ - refresh-токен.

     Но по нему можно будет искать.
     */
    @Indexed
    private UUID sessionId;

    @Indexed // Чтобы можно было найти все токены юзера
    private Long userId;

    @Indexed
    private Instant expiresAt;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long timeToLive; // в секундах

    @Indexed
    private String accessToken;

    @Indexed
    private String refreshToken;

    @Indexed
    private String fingerprint;

    @Indexed
    private String roles;

    @Builder
    public ActiveSessionCache(
            String fingerprintHash,
            Long userId,
            UUID sessionId,
            String accessToken,
            String refreshToken,
            String roles, // Добавляем в конструктор
            Duration ttl
    ) {
        this.fingerprintHash = fingerprintHash;
        this.userId = userId;
        this.sessionId = sessionId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.roles = roles;
        this.timeToLive = ttl.toSeconds();
    }
}