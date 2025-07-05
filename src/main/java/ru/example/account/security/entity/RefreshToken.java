package ru.example.account.security.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import java.io.Serial;

@Getter
@Builder
@RedisHash("refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private UUID sessionId;

    @Indexed
    private Long userId;

    @Indexed
    private String tokenRefresh;

    @Indexed
    private Instant expiryDate;
}