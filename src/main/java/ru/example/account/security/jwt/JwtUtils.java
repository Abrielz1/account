package ru.example.account.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
import ru.example.account.shared.exception.exceptions.InvalidJwtAuthenticationException;
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
    private String rawSecret;

    @Value("${app.jwt.token-expiration}")
    private Duration tokenExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (rawSecret == null || rawSecret.isBlank()) {
            throw new IllegalArgumentException("JWT secret is not configured in application properties (app.jwt.secret)");
        }
        try {
            byte[] decodedKey = Base64.getDecoder().decode(rawSecret);
            this.secretKey = new SecretKeySpec(decodedKey, "HmacSHA512");
            log.info("JWT secret key initialized successfully.");
        } catch (Exception e) {
            log.error("Invalid JWT secret key. It must be a Base64-encoded string.", e);
            throw new IllegalArgumentException("Invalid JWT secret key.", e);
        }
    }

    public String generateAccessToken(AppUserDetails userDetails, UUID sessionId) {
        Instant now = Instant.now();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getEmail())
                .claim("userId", userDetails.getId())
                .claim("roles", roles)
                .id(sessionId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenExpiration)))
                .signWith(secretKey)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage()); // Меняем на DEBUG, так как это штатная ситуация
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error parsing JWT claims", e);
            throw new InvalidJwtAuthenticationException("Failed to parse JWT claims");
        }
    }

    public UUID getSessionId(Claims claims) {
        return UUID.fromString(claims.getId());
    }

    public Long getUserId(Claims claims) {
        return claims.get("userId", Long.class);
    }

    public String getEmail(Claims claims) {
        return claims.getSubject();
    }

    public Instant getExpiration(Claims claims) {
        return claims.getExpiration().toInstant();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoleClaims(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .map(SimpleGrantedAuthority::getAuthority)
                .toList();
    }

    public List<? extends GrantedAuthority> getAuthorities(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
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
