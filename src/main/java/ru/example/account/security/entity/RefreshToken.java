package ru.example.account.security.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import java.io.Serial;

@Builder
@Getter
@RedisHash("refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
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

    @TimeToLive
    private Long timeToLive; // в секундах

//    @Builder
//    public static RefreshToken create(UUID sessionId, Long userId, Duration ttl) {
//        RefreshToken rt = new RefreshToken();
//        rt.token = UUID.randomUUID().toString();
//        rt.sessionId = sessionId;
//        rt.userId = userId;
//        rt.timeToLive = ttl.toSeconds();
//        rt.expiresAt = Instant.now().plus(ttl);
//        return rt;
//    }
}