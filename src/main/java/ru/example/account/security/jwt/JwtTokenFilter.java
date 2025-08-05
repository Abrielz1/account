package ru.example.account.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.service.BlacklistService;
import ru.example.account.security.service.FingerprintService;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.SessionRevocationService;
import ru.example.account.security.service.TrustedDeviceService;
import ru.example.account.shared.exception.exceptions.InvalidJwtAuthenticationException;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private final BlacklistService blacklistService;

    private final TrustedDeviceService trustedDeviceService;

    private final SessionQueryService sessionQueryService;

    private final UserDetailsService userDetailsService;

    private final FingerprintService fingerprintService;   // --- ДОБАВЛЯЕМ "ТЯЖЕЛУЮ АРТИЛЛЕРИЮ"! ---

    private final SessionRevocationService sessionRevocationService; // <<<--- КЛЮЧЕВАЯ ЗАВИСИМОСТЬ ДЛЯ ФСБ и СБ

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException {

        try {
            final String token = extractTokenFromRequest(request);
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // --- ШАГ 1 (ТВОЙ): "КОРРЕКТНЫЙ ЛИ ТОКЕН?" (самая быстрая проверка) ---
            if (!jwtUtils.isTokenValid(token)) {
                this.applySecurityDelay();
                throw new InvalidJwtAuthenticationException("Token is expired or has invalid signature");
            }

            // --- ШАГ 2: "ГОРЯЧИЕ" ПРОВЕРКИ В REDIS ---
            final Claims claims = jwtUtils.getAllClaimsFromToken(token);
            final Long userId = jwtUtils.getUserId(claims);
            final String currentFingerprint = fingerprintService.generateUsersFingerprint(request);

            // 2a. Черный список
            if (blacklistService.isAccessTokenBlacklisted(token)) {

                // 1. УНИЧТОЖАЕМ ВСЕ СЕССИИ ЭТОГО ПОЛЬЗОВАТЕЛЯ
                this.sessionRevocationService.revokeAllSessionsForUser(
                        userId,
                        SessionStatus.STATUS_COMPROMISED,
                        RevocationReason.REASON_RED_ALERT
                );
                throw new SecurityException("Access denied for blacklisted token.");
            }

            // 2b. Белый список
            if (!trustedDeviceService.isDeviceTrusted(userId, currentFingerprint)) {
                throw new SecurityException("Access denied from an untrusted device.");
            }

            // --- ШАГ 3 "ПОДНЯТЬ СЕССИЮ И СВЕРИТЬ ВСЁ С НЕЙ" (холодная проверка) ---
            Optional<AuthSession> sessionOpt = sessionQueryService.findActiveByAccessToken(token);
            if (sessionOpt.isEmpty()) {
                // Если сессии нет в БД, хотя токен валиден и не в черном списке - это подозрительно.
                throw new SecurityException("No active session found for the provided access token.");
            }

            AuthSession sessionFromDb = sessionOpt.get();
            String hashFromDb = sessionFromDb.getFingerprintHash();
            String hashFromRequest = jwtUtils.createFingerprintHash(currentFingerprint);

            if (!Objects.equals(hashFromDb, hashFromRequest)) {
                // RED ALERT!
                this.sessionRevocationService.revokeAllSessionsForUser(
                        userId,
                        SessionStatus.STATUS_COMPROMISED,
                        RevocationReason.REASON_RED_ALERT
                );
                throw new SecurityBreachAttemptException("Token-Device binding validation failed!");
            }

            // --- УСПЕХ! ---
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Мы можем взять email из claims, т.к. мы уже ПОЛНОСТЬЮ доверяем этому токену
                UserDetails userDetails = userDetailsService.loadUserByUsername(jwtUtils.getEmail(claims));
                setAuthentication(request, userDetails);
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            handleAuthError(response, ex);
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleAuthError(HttpServletResponse response, Exception e) throws IOException {
        String errorId = "AUTH-ERR-" + System.currentTimeMillis();
        log.error("Authentication error [{}]: {}", errorId, e.getMessage(), e);
        response.setHeader("X-Error-ID", errorId);
        int status = (e instanceof SecurityException || e instanceof SecurityBreachAttemptException)
                ? HttpServletResponse.SC_FORBIDDEN
                : HttpServletResponse.SC_UNAUTHORIZED;
        response.sendError(status, "Authentication failed. Ref: " + errorId);
    }

    private void applySecurityDelay() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}