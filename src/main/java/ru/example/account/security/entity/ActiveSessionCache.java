package ru.example.account.security.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ActiveSessionCache implements Serializable {

    private String fingerprintHash;

    private UUID sessionId;

    private Long userId;

    private Instant expiresAt;

    private Long ttl; // в секундах

    private String accessToken;

    private String refreshToken;

    private String roles;

    @Builder
    public ActiveSessionCache(
            String fingerprintHash,
            Long userId,
            UUID sessionId,
            String accessToken,
            String refreshToken,
            String roles,
            Instant expiresAt,
            Duration ttl
    ) {
        this.fingerprintHash = fingerprintHash;
        this.userId = userId;
        this.sessionId = sessionId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.roles = roles;
        this.expiresAt = expiresAt;
        this.ttl = ttl.toSeconds();
    }
}