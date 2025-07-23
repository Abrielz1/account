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
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.service.AuthService;
import ru.example.account.security.service.FingerprintService;
import ru.example.account.security.service.HttpUtilsService;
import ru.example.account.security.service.SessionServiceManager;
import ru.example.account.security.service.TimezoneService;
import ru.example.account.security.service.UserService;
import ru.example.account.shared.exception.exceptions.UserNotVerifiedException;
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


        // Проверка, что аккаунт пользователя активирован (после подтверждения email)
        if (!currentUser.isEnabled()) {
            throw new UserNotVerifiedException("User account is not active. Please check your email, especially spam folder.");
        }

        log.info("User {} successfully authenticated.", currentUser.getUsername());

        String fingerprint = this.fingerprintService.generateUsersFingerprint(httpRequest);
        String ipAddress = this.httpUtilsService.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        ZonedDateTime lastSeenAt = ZonedDateTime.now();

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

        log.info("Token refresh process started.");
        AppUserDetails currentUser = (AppUserDetails) userDetailsService.loadUserByUsername(request.username());
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
