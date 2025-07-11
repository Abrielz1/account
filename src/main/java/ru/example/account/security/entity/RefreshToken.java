package ru.example.account.security.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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
public class RefreshToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String token;

    @Indexed
    private UUID sessionId;

    @Indexed // Чтобы можно было найти все токены юзера
    private Long userId;

    @Indexed
    private Instant expiresAt;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long timeToLive; // в секундах

    @Builder
    private RefreshToken(UUID sessionId, Long userId, String token, Duration ttl) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.token = token;
        this.expiresAt = Instant.now().plus(ttl);
        this.timeToLive = ttl.toSeconds();
    }
}