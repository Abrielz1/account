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

@Data
@Builder
@RedisHash("refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken implements Serializable {

    @Id
    @Indexed
    private Long id;

    @Indexed
    private Long userId;

    @Indexed
    private String tokenRefresh;

    @Indexed
    private Instant expiryDate;
}
