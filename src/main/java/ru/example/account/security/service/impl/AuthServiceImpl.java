package ru.example.account.security.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.service.AccessTokenBlacklistService;
import ru.example.account.security.service.AuthService;
import ru.example.account.security.service.FingerprintService;
import ru.example.account.security.service.HttpUtilsService;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.SessionRevocationService;
import ru.example.account.security.service.SessionCommandService;
import ru.example.account.security.service.SessionServiceManager;
import ru.example.account.security.service.TimezoneService;
import ru.example.account.security.service.UserService;
import ru.example.account.shared.exception.exceptions.UserNotVerifiedException;
import ru.example.account.shared.util.FingerprintUtils;
import ru.example.account.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // --- ЗАВИСИМОСТИ ---
    private final AuthenticationManager authenticationManager;

    private final IdGenerationServiceImpl idGenerationService;

    private final JwtUtils jwtUtils;

    private final SessionQueryService sessionQueryService;

    private final SessionRevocationService sessionRevocationService;

    private final SessionCommandService sessionService;

    private final AccessTokenBlacklistService blacklistService;

    private final FingerprintUtils fingerprintUtils;

    private final ApplicationEventPublisher eventPublisher;

    private final UserDetailsService userDetailsService;

    private final UserRepository userRepository;

    private final SessionServiceManager sessionManager; // главный

    private final UserService userService;             // Для обновления last_login

    private final FingerprintService fingerprintService;

    private final HttpUtilsService httpUtilsService;

    private final TimezoneService timezoneService;

    private final AuthSessionRepository authSessionRepository;

    @Override
    @Transactional(value = "securityTransactionManager")
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

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();


        // Проверка, что аккаунт пользователя активирован (после подтверждения email)
        if (!userDetails.isEnabled()) {
            throw new UserNotVerifiedException("User account is not active. Please check your email, especially spam folder.");
        }

        log.info("User {} successfully authenticated.", userDetails.getUsername());


        String ipAddress = this.httpUtilsService.getClientIpAddress(httpRequest);

        String fingerprint = this.fingerprintService.generateUsersFingerprint(httpRequest);

        String userAgent = httpRequest.getHeader("User-Agent");

        this.userService.updateLastLoginAsync(userDetails.getId(), this.timezoneService.getZoneIdFromRequest(httpRequest));

        return  this.sessionManager.createSession(
                userDetails,
                ipAddress,
                fingerprint,
                userAgent);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {

        log.info("Token refresh process started.");
        AppUserDetails currentUser = (AppUserDetails) userDetailsService.loadUserByUsername(request.username());
        return this.sessionManager.rotateSessionAndTokens(
                request.refreshToken(),
                request.accessesToken(), // Передаем и старый access-token
                this.fingerprintService.generateUsersFingerprint(httpRequest),
                currentUser
        );
    }

    @Override
    public void logout(AppUserDetails userDetails) {

    }

    @Override
    public void logoutAll(Long userId) {

    }
}
