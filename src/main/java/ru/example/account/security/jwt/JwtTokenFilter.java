package ru.example.account.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.example.account.security.entity.EventType;
import ru.example.account.security.entity.SecurityEvent;
import ru.example.account.security.service.temp.AccessTokenBlacklistService;
import ru.example.account.security.service.impl.AppUserDetails;
import ru.example.account.user.entity.RoleType;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final AccessTokenBlacklistService blacklistService;
    private final ApplicationEventPublisher eventPublisher;

    private static final Set<String> VALID_ROLES = Arrays.stream(RoleType.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        if (!jwtUtils.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final Claims claims = jwtUtils.getAllClaimsFromToken(token);
            final UUID sessionId = jwtUtils.getSessionId(claims);
            final Long userId = jwtUtils.getUserId(claims);

            if (blacklistService.isBlacklisted(sessionId)) {
                log.warn("Access denied for blacklisted session ID: {}", sessionId);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session has been terminated.");
                return;
            }

            if (this.isPayloadSuspicious(claims)) {
                this.handleSuspiciousToken(request, userId, sessionId, claims);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token payload.");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                this.setAuthentication(request, claims);
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(HttpServletRequest request, Claims claims) {
        UserDetails userDetails = new AppUserDetails(
                jwtUtils.getUserId(claims),
                jwtUtils.getEmail(claims),
                jwtUtils.getAuthorities(claims),
                jwtUtils.getSessionId(claims),
                jwtUtils.getExpiration(claims)
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isPayloadSuspicious(Claims claims) {
        Long userId = jwtUtils.getUserId(claims);
        if (userId == null || userId <= 0) return true;
        List<String> rolesInToken = jwtUtils.getRoleClaims(claims);
        return rolesInToken == null || rolesInToken.isEmpty() || !VALID_ROLES.containsAll(rolesInToken);
    }

    private void handleSuspiciousToken(HttpServletRequest request, Long userId, UUID sessionId, Claims claims) {
        log.error("!!! SECURITY ALERT: Suspicious token detected. IP: {}, User-Agent: {}, UserID: {}, Claims: {}",
                request.getRemoteAddr(), request.getHeader("User-Agent"), userId, claims.toString());

        final String ipAddress = request.getRemoteAddr();
        final String userAgent = request.getHeader("User-Agent");

        eventPublisher.publishEvent(new SecurityEvent(
                EventType.TOKEN_PAYLOAD_SUSPICIOUS,
                userId,
                sessionId,
                ipAddress,
                userAgent,
                Map.of("claims", claims.toString())
        ));
    }
}