package ru.example.account.app.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@Setter
@Getter
public class JwtUtils {

    @Autowired
    private Environment env;

    @Value("${app.jwt.secret}")
    protected String rawSecret;

    @Value("${app.jwt.tokenExpiration}")
    protected Duration tokenExpiration;

    protected SecretKey secretKey;


   @PostConstruct
    public void init() {
       log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
       log.info("Raw secret assigned: {}", rawSecret);
        try {
            if (rawSecret.contains("-")) {
                rawSecret = "fQL3RmtrMkHctNXq6ckD0xxm19OmglQqH0fWXWjdPVFjH43CaJmEWaKxe/04gBrCdDaCXmhxwxQSaBb0i6xUyg==";
            }
            System.out.println(rawSecret.equals("fQL3RmtrMkHctNXq6ckD0xxm19OmglQqH0fWXWjdPVFjH43CaJmEWaKxe/04gBrCdDaCXmhxwxQSaBb0i6xUyg=="));
            byte[] keyBytes = Base64.getDecoder().decode(rawSecret);

            if (!rawSecret.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$")) {
                throw new IllegalStateException("Incorrect MIME encoding");
            }

            if (keyBytes.length != 64) {
                throw new IllegalStateException("Invalid key length: expected 64 bytes but got " + keyBytes.length);
            }

            this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
            log.info("JWT initialized successfully with {} bit key", keyBytes.length * 8);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid Base64 encoding for JWT secret: " + e.getMessage(), e);
        }
    }

    public String generateTokenFromUsername(String username, Long userId) {
        Instant iat = Instant.now();
        Date exp = Date.from(iat.plus(tokenExpiration));

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(Date.from(Instant.from(iat)))
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String accessToken) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getSubject();
    }

    public Boolean validateAccessToken(String accessToken) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException expEx) {
            log.error("Expired or unsupported JWT", expEx);
        } catch (MalformedJwtException expEx) {
            log.error("Malformed JWT", expEx);
        } catch (SecurityException expEx) {
            log.error("Security exception", expEx);
        } catch (Exception expEx) {
            log.error("Invalid token", expEx);
        }
        return false;
    }

    public Long getUserIdFromClaimJwt(String token) {
        return Jwts.parser().verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Long.class);
    }
}
