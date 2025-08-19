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
import ru.example.account.security.service.AuthService;
import ru.example.account.security.service.worker.FingerprintService;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.facade.SessionRevocationServiceFacade;
import ru.example.account.shared.exception.exceptions.DeviceNotVerifiedException;
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

  //  private final BlacklistService blacklistService;

  //  private final WhitelistService trustedDeviceService;

    private final SessionQueryService sessionQueryService;

    private final UserDetailsService userDetailsService;

    private final FingerprintService fingerprintService;   // --- ДОБАВЛЯЕМ "ТЯЖЕЛУЮ АРТИЛЛЕРИЮ"! ---

    private final SessionRevocationServiceFacade sessionRevocationService; // <<<--- КЛЮЧЕВАЯ ЗАВИСИМОСТЬ ДЛЯ ФСБ и СБ

    private final AuthService authService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException {

        boolean isHasToken = false; // тоены оба вообще есть в jwt?
        boolean isTokenValid = false; //срок, подпись
        boolean isDeviceKnown = false; //белый список
        boolean isTokenBlacklisted = false; // в чёрном ли списке?
        boolean isTokenBindingCorrect = false; // сверка хэшей

        /**
         * ЕСЛИ (!hasToken && !isDeviceKnown) -> Ответ: "Ты — "первопроходец". Иди регистрируйся/логинься". (Статус: Normal`)
         *ЕСЛИ (!hasToken && isDeviceKnown) -> Ответ: "Я тебя помню, но сессия "протухла". Иди логинься". (Статус: Normal`)
         * ЕСЛИ (hasToken && !isDeviceKnown) -> **Ответ: "ОПА-НА. У тебя ЕСТЬ наш "секретный пропуск",
         * но ты стучишься из "непонятного подвала". Это—RED ALERT**. (Статус: **_SECURITY BREACH_**), фсб ТЕБЕ в хату.
         */


        try {
            final String token = extractTokenFromRequest(request);
            if (token == null) {
                log.trace("emty token wasgiven into doFilterInternal");
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
            final String currentFingerprintHash = this.jwtUtils.createFingerprintHash(currentFingerprint);
            final String givenFingerPrintHash = this.jwtUtils.getFingerprintHash(claims);

            // --- ШАГ 2,5: "ГОРЯЧИЕ" Фингер принтов, на наличие и одинаковость ---
            if (!StringUtils.hasText(currentFingerprint) && !StringUtils.hasText(givenFingerPrintHash) && !Objects.equals(currentFingerprintHash, givenFingerPrintHash)) {
                this.sessionRevocationService.revokeAllSessionsForUser(
                        userId,
                        SessionStatus.STATUS_COMPROMISED,
                        RevocationReason.REASON_RED_ALERT
                );
                throw new SecurityException("Access denied for blacklisted token.");
            }

            // 2a. Черный список
//            if (this.blacklistService.isAccessTokenBlacklisted(token)) {
//
//                // 1. УНИЧТОЖАЕМ ВСЕ СЕССИИ ЭТОГО ПОЛЬЗОВАТЕЛЯ
//                this.sessionRevocationService.revokeAllSessionsForUser(
//                        userId,
//                        SessionStatus.STATUS_COMPROMISED,
//                        RevocationReason.REASON_RED_ALERT
//                );
//                throw new SecurityException("Access denied for blacklisted token.");
//            }

            // 2b. Белый список todo переделать логику вызова
//            if (!this.trustedDeviceService.isDeviceTrusted(userId, token, currentFingerprint)) {
//                this.sessionRevocationService.revokeAllSessionsForUser(
//                        userId,
//                        SessionStatus.STATUS_COMPROMISED,
//                        RevocationReason.REASON_RED_ALERT
//                );
//                throw new SecurityException("Access denied from an untrusted device.");
//            }

            // --- ШАГ 3 "ПОДНЯТЬ СЕССИЮ И СВЕРИТЬ ВСЁ С НЕЙ" (холодная проверка) ---
            Optional<AuthSession> sessionOpt = sessionQueryService.findActiveByAccessToken(token);
            if (sessionOpt.isEmpty()) {
                // Если сессии нет в БД, хотя токен валиден и не в черном списке - это подозрительно.
                throw new SecurityException("No active session found for the provided access token.");
            }

            final AuthSession sessionFromDb = sessionOpt.get();
            final String hashFromDb = sessionFromDb.getFingerprintHash();
            final String hashFromRequest = jwtUtils.createFingerprintHash(currentFingerprint);

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
                final UserDetails userDetails = userDetailsService.loadUserByUsername(jwtUtils.getEmail(claims));
                this.setAuthentication(request, userDetails);
            }

            filterChain.doFilter(request, response);

        } catch (DeviceNotVerifiedException e) {
        // --- ВОТ ОН, НАШ "ИНТЕЛЛЕКТ"! ---
        // "Ага, это НЕ атака. Это - наш юзер с нового устройства".
        // Запускаем ПРОЦЕСС верификации.
        this.authService.trustDevice(e.getUserId(), e.getFingerPrint(), request);

        // И отдаем фронтенду ЧЕТКИЙ, ПОНЯТНЫЙ приказ.
        response.sendError(
                HttpServletResponse.SC_FORBIDDEN, // 403 Forbidden, но...
                "DEVICE_NOT_VERIFIED" // ... с "кодом", который поймет фронтенд.
        );
        SecurityContextHolder.clearContext();

    } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            this.handleAuthError(response, ex);
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
        final String errorId = "AUTH-ERR-" + System.currentTimeMillis();
        log.error("Authentication error [{}]: {}", errorId, e.getMessage(), e);
        response.setHeader("X-Error-ID", errorId);
        final int status = (e instanceof SecurityException || e instanceof SecurityBreachAttemptException)
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