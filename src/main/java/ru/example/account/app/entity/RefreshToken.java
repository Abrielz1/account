package ru.example.account.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import java.io.Serial;

@Data
@Builder
@RedisHash("refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
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