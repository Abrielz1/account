package ru.example.account.security.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.redis")
public class RedisKeysProperties {

    @NotNull // Весь блок "keys" должен быть
    private Keys keys = new Keys();

    @NotNull // Весь блок "ttl" должен быть
    private Ttl ttl = new Ttl();

    @Getter
    @Setter
    public static class Keys {

        @NotNull
        private Blacklist blacklist = new Blacklist();

        @NotNull
        private Whitelist whitelist = new Whitelist();

        @NotNull
        private Verification verification = new Verification();

        @NotNull
        private String activeSessionPrefix;
    }

    @Getter
    @Setter
    public static class Blacklist {

        @NotBlank
        private String accessTokenPrefix;

        @NotBlank
        private String refreshTokenPrefix;
    }

    @Getter
    @Setter
    public static class Whitelist {

        @NotBlank
        private String fingerprintKeyFormat;
    }

    @Getter
    @Setter
    public static class Verification {

        @NotBlank
        private String deviceCodeFormat;
    }

    @Getter
    @Setter
    public static class Ttl {

        @NotNull
        private Duration activeSession;

        @NotNull
        private Duration bannedAccessToken;

        @NotNull
        private Duration bannedRefreshToken;

        @NotNull
        private Duration trustedFingerprint;

        @NotNull
        private Duration deviceVerificationCode;
    }
}
