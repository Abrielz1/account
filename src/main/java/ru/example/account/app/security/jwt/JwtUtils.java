package ru.example.account.app.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String someSecretKey;

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

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
                .signWith(this.generateKey())
                .compact();
    }

    public String getUsernameFromToken(String accessToken) {

        return Jwts.parser()
                .verifyWith(this.getSignKey())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getSubject();
    }

    public Boolean validateAccessToken(String accessToken) {

        try {
            Jwts.parser()
                    .verifyWith(this.getSignKey())
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

    private Key generateKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(someSecretKey));
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(someSecretKey));
    }

    public Long getUserIdFromClaimJwt(String token) {

        return Jwts.parser().verifyWith(this.getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Long.class);
    }
}
