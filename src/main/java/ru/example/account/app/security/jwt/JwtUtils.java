package ru.example.account.app.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    private final Environment env;


    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    private SecretKey secretKey;

    public JwtUtils(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        try {
            // Удаляем все непечатаемые символы
            String rawSecret =  "KOEqxmVGbQPbephZ75sK5J6bZVN1OJWzO2hKRZ3mVDVCdVJ/Oa56aG5D5RNH3l087J3Sz2iPoiCp0gVMulSbgw==";//env.getProperty("app.jwt.secret");

            if (rawSecret.isBlank()) {
                throw new IllegalStateException("JWT secret is not configured");
            }

            String cleanSecret = rawSecret.replace("\"", "")
                    .replace("'", "")
                    .trim();

            log.info("Initializing JWT with secret: {}", cleanSecret.substring(0, 10) + "...");

            log.info("Cleaned JWT secret: [{}]", cleanSecret);

            byte[] keyBytes = Base64.getDecoder().decode(cleanSecret);
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
            log.info("JWT initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize JWT secret", e);
            throw new RuntimeException("JWT initialization failed", e);
        }
    }

    public String generateJwtToken(String username, Long userId) {
        return this.generateTokenFromUsername(username, userId);
    }

    public String generateTokenFromUsername(String username, Long userId) {

        Instant iat = Instant.now();
        Date exp = Date.from(iat.plus(tokenExpiration));

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(Date.from(Instant.from(iat)))
                .expiration(exp)
                .signWith(secretKey) //this.generateKey()
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
                    .verifyWith(secretKey)//this.getSignKey
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException expEx) {
            log.error("Expired JwtException", expEx);
        } catch (MalformedJwtException expEx) {
            log.error("Malformed JwtException", expEx);
        } catch (SecurityException expEx) {
            log.error("Security Exception", expEx);
        } catch (Exception expEx) {
            log.error("invalid token", expEx);
        }

        log.error("not valid token!");
        return false;
    }

//    private Key generateKey() {
//        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(someSecretKey));
//    }

    //  private SecretKey getSignKey() {
    //   return Keys.hmacShaKeyFor(Decoders.BASE64.decode(someSecretKey));
    //  }

    public Long getUserIdFromClaimJwt(String token) {

        return Jwts.parser().verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Long.class);
    }
}
