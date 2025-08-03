package ru.example.account.security.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.redis")
public class RedisKeysProperties {

    private Keys keys = new Keys();
    private Ttl ttl = new Ttl();

    @Getter
    @Setter
    public static class Keys {
        private Blacklist blacklist = new Blacklist();
        private Whitelist whitelist = new Whitelist();
        private Verification verification = new Verification();
    }

    @Getter
    @Setter
    public static class Blacklist {
        private String accessTokenPrefix;
        private String refreshTokenPrefix;
    }

    @Getter
    @Setter
    public static class Whitelist {
        private String fingerprintKeyFormat;
    }

    @Getter
    @Setter
    public static class Verification {
        private String deviceCodeFormat;
    }

    @Getter
    @Setter
    public static class Ttl {
        private Duration bannedAccessToken;
        private Duration bannedRefreshToken;
        private Duration trustedFingerprint;
        private Duration deviceVerificationCode;
    }
}
