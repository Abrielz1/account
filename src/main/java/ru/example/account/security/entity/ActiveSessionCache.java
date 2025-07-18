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
public class ActiveSessionCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**

     САМ refresh-токен является первичным ключом в этом хранилище.
     */
    @Id
    private String refreshTokenValue;
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
    private String fingerprintHash;

    @Builder
    private ActiveSessionCache(UUID sessionId, Long userId, String refreshTokenValue, Duration ttl, String accessToken, String fingerprintHash) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.refreshTokenValue = refreshTokenValue;
        this.expiresAt = Instant.now().plus(ttl);
        this.timeToLive = ttl.toSeconds();
        this.accessToken = accessToken;
        this.fingerprintHash = fingerprintHash;
    }
}