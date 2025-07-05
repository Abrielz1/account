package ru.example.account.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import ru.example.account.security.service.impl.AppUserDetails;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.token-expiration}")
    private Duration tokenExpiration;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(secret);
            this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA512");
            log.info("JWT secret key initialized successfully.");
        } catch (Exception e) {
            log.error("Invalid JWT secret key. Please check your configuration. Key must be a Base64-encoded string.", e);
            throw new IllegalArgumentException("Invalid JWT secret key.");
        }
    }

    /**
     * Генерирует Access Token, содержащий все необходимые claims.
     */
    public String generateAccessToken(AppUserDetails userDetails, UUID sessionId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(tokenExpiration);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getEmail())
                .claim("userId", userDetails.getId())
                .claim("roles", roles)
                .id(sessionId.toString()) // Используем стандартный claim 'jti' для sessionId
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Валидирует токен (подпись и срок жизни).
     * @return true, если токен валиден.
     */
    public boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Извлекает все claims из токена.
     * ВАЖНО: Этот метод следует вызывать ТОЛЬКО ПОСЛЕ validate(token).
     */
    public Claims getAllClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public UUID getSessionId(String token) {
        String jti = this.getAllClaims(token).getId();
        return UUID.fromString(jti);
    }

    public Long getUserId(String token) {
        return this.getAllClaims(token).get("userId", Long.class);
    }

    public List<String> getRoleStrings(String token) {

       List<String> roles = this.getRoleStrings(token);
            if (roles == null) {
                return Collections.emptyList();
            }
            return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .map(SimpleGrantedAuthority::getAuthority)
                    .toList();
    }
}

//package ru.example.account.security.jwt;
//
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.MalformedJwtException;
//import io.jsonwebtoken.UnsupportedJwtException;
//import jakarta.annotation.PostConstruct;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.Arrays;
//import java.util.Base64;
//import java.util.Date;
//
//@Slf4j
//@Component
//@Setter
//@Getter
//public class JwtUtils {
//
//    @Autowired
//    private Environment env;
//
//    @Value("${app.jwt.secret}")
//    protected String rawSecret;
//
//    @Value("${app.jwt.tokenExpiration}")
//    protected Duration tokenExpiration;
//
//    protected SecretKey secretKey;
//
//
//   @PostConstruct
//    public void init() {
//       log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
//       log.info("Raw secret assigned: {}", rawSecret);
//        try {
//            if (rawSecret.contains("-")) {
//                rawSecret = "fQL3RmtrMkHctNXq6ckD0xxm19OmglQqH0fWXWjdPVFjH43CaJmEWaKxe/04gBrCdDaCXmhxwxQSaBb0i6xUyg==";
//            }
//            System.out.println(rawSecret.equals("fQL3RmtrMkHctNXq6ckD0xxm19OmglQqH0fWXWjdPVFjH43CaJmEWaKxe/04gBrCdDaCXmhxwxQSaBb0i6xUyg=="));
//            byte[] keyBytes = Base64.getDecoder().decode(rawSecret);
//
//            if (!rawSecret.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$")) {
//                throw new IllegalStateException("Incorrect MIME encoding");
//            }
//
//            if (keyBytes.length != 64) {
//                throw new IllegalStateException("Invalid key length: expected 64 bytes but got " + keyBytes.length);
//            }
//
//            this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
//            log.info("JWT initialized successfully with {} bit key", keyBytes.length * 8);
//        } catch (IllegalArgumentException e) {
//            throw new IllegalStateException("Invalid Base64 encoding for JWT secret: " + e.getMessage(), e);
//        }
//    }
//
//    public String generateTokenFromUsername(String username, Long userId) {
//        Instant iat = Instant.now();
//        Date exp = Date.from(iat.plus(tokenExpiration));
//
//        return Jwts.builder()
//                .subject(username)
//                .claim("userId", userId)
//                .issuedAt(Date.from(Instant.from(iat)))
//                .expiration(exp)
//                .signWith(secretKey)
//                .compact();
//    }
//
//    public String getUsernameFromToken(String accessToken) {
//        return Jwts.parser()
//                .verifyWith(secretKey)
//                .build()
//                .parseSignedClaims(accessToken)
//                .getPayload()
//                .getSubject();
//    }
//
//    public Boolean validateAccessToken(String accessToken) {
//        try {
//            Jwts.parser()
//                    .verifyWith(secretKey)
//                    .build()
//                    .parseSignedClaims(accessToken)
//                    .getPayload();
//            return true;
//        } catch (ExpiredJwtException | UnsupportedJwtException expEx) {
//            log.error("Expired or unsupported JWT", expEx);
//        } catch (MalformedJwtException expEx) {
//            log.error("Malformed JWT", expEx);
//        } catch (SecurityException expEx) {
//            log.error("Security exception", expEx);
//        } catch (Exception expEx) {
//            log.error("Invalid token", expEx);
//        }
//        return false;
//    }
//
//    public Long getUserIdFromClaimJwt(String token) {
//        return Jwts.parser().verifyWith(secretKey)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .get("userId", Long.class);
//    }
//}
