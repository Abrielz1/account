package ru.example.account.security.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.jwt.JwtTokenFilter;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.service.AuthService;
import ru.example.account.security.service.FingerprintService;
import ru.example.account.security.service.HttpUtilsService;
import ru.example.account.security.service.SessionRevocationService;
import ru.example.account.security.service.SessionServiceManager;
import ru.example.account.security.service.TimezoneService;
import ru.example.account.security.service.UserService;
import ru.example.account.shared.exception.exceptions.BadRequestException;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;
import ru.example.account.shared.exception.exceptions.UserNotVerifiedException;
import ru.example.account.shared.util.SecurityContextValidator;

import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDetailsService userDetailsService;

    private final AuthenticationManager authenticationManager;

    private final SessionServiceManager sessionManager; // главный

    private final UserService userService;             // Для обновления last_login

    private final FingerprintService fingerprintService;

    private final HttpUtilsService httpUtilsService;

    private final TimezoneService timezoneService;

    private final SecurityContextValidator contextValidator;

    private final AuthSessionRepository authSessionRepository;

    private final SessionRevocationService sessionRevocationService;

    private final JwtUtils jwtUtils;

    private final JwtTokenFilter jwtTokenFilter;

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {

        log.info("Authentication attempt for user: {}", request.email());

        Authentication authentication;
        try {
            authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                ));
        } catch (BadCredentialsException e) {
            log.warn("Failed login for [{}]: Bad credentials", request.email());

            throw e;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUserDetails currentUser = (AppUserDetails) authentication.getPrincipal();

        log.info("User {} successfully authenticated.", currentUser.getUsername());

        String fingerprint = this.fingerprintService.generateUsersFingerprint(httpRequest);
        String ipAddress = this.httpUtilsService.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        ZonedDateTime lastSeenAt = ZonedDateTime.now();

        // ВАЛИДИРУЕМ ВЕСЬ КОНТЕКСТ СРАЗУ!
        this.contextValidator.validate(currentUser, fingerprint, ipAddress);
        this.userService.updateLastLoginAsync(currentUser.getId(), this.timezoneService.getZoneIdFromRequest(httpRequest));

        return  this.sessionManager.createSession(
                currentUser,
                ipAddress,
                fingerprint,
                userAgent,
                lastSeenAt);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {

        if (!StringUtils.hasText(request.accessesToken()) && !StringUtils.hasText(request.refreshToken())) {
            log.trace("User send empty tokens");
            throw new BadRequestException("User send empty tokens");
        }

        log.info("Token refresh process started.");
        AppUserDetails currentUser = (AppUserDetails) userDetailsService.loadUserByUsername(request.username());

       String token = "";

       if (request.accessesToken().startsWith("Bearer")) {
          token = request.accessesToken().substring(7);
       } else {
           token = request.accessesToken();
       }

        Boolean refreshTokenIsExists = this.authSessionRepository.existsByRefreshToken(request.refreshToken());
        Boolean accessTokenIsExists = this.jwtUtils.isTokenValid(token);

        if (Boolean.TRUE.equals(!refreshTokenIsExists) && Boolean.TRUE.equals(!accessTokenIsExists)) {
            log.trace("RED ALERT! A HACKER TRIES BREACH SYSTEM");
            ///Revoke all session
            this.sessionRevocationService.revokeAllSessionsForUser(currentUser.getId(),
                    SessionStatus.STATUS_COMPROMISED,
                    RevocationReason.REASON_RED_ALERT);
            throw new SecurityBreachAttemptException("RED ALERT! A HACKER TRIES BREACH SYSTEM");
        }

        String ipAddress = this.httpUtilsService.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return this.sessionManager.rotateSessionAndTokens(
                request.refreshToken(),
                request.accessesToken(), // Передаем и старый access-token
                this.fingerprintService.generateUsersFingerprint(httpRequest),
                ipAddress,
                userAgent,
                currentUser
        );
    }

    @Override
    public void logout(AppUserDetails userDetails) {

        this.sessionManager.logout(userDetails);
    }

    @Override
    public void logoutAll(AppUserDetails userToLogOut) {

        this.sessionManager.logoutAll(userToLogOut);
    }
}
