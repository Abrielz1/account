package ru.example.account.security.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.service.AccessTokenBlacklistService;
import ru.example.account.security.service.AuthService;
import ru.example.account.security.service.SessionArchivingService;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;
import ru.example.account.shared.exception.exceptions.TokenRefreshException;
import ru.example.account.shared.util.FingerprintUtils;
import ru.example.account.user.entity.User;
import ru.example.account.user.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final AuthSessionRepository authSessionRepository;

    private final UserRepository userRepository;

    private final JwtUtils jwtUtils;

    private final AccessTokenBlacklistService blacklistService;

    private final SessionArchivingService sessionArchivingService; // Новый сервис для архивации

    private final ApplicationEventPublisher eventPublisher;

    private final FingerprintUtils fingerprintUtils; // Получение слепка, не реализовано

    @Value("${app.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
    @Transactional("securityTransactionManager")
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        // Создаем и сохраняем новую сессию в Postgres
        String fingerprint = this.fingerprintUtils.generate(httpRequest); // Генерируем "отпечаток"
        AuthSession session = AuthSession.builder()
                .id(UUID.randomUUID()) // sessionId
                .userId(userDetails.getId())
                .refreshToken(UUID.randomUUID().toString())
                .status(SessionStatus.STATUS_ACTIVE)
                .fingerprintHash(fingerprint)
                .ipAddress(httpRequest.getRemoteAddr())
                .userAgent(httpRequest.getHeader("User-Agent"))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(refreshTokenExpiration))
                .build();
        authSessionRepository.save(session);
        log.info("New auth session created for user {}. Session ID: {}", userDetails.getId(), session.getId());

        // Генерируем токены
        String accessToken = jwtUtils.generateAccessToken(userDetails, session.getId());
        return new AuthResponse(userDetails.getId(),
                                            accessToken,
                                            session.getRefreshToken(),
                                            userDetails.getUsername(),
                                            new ArrayList<>()); // Дописать!
    }

    @Override
    @Transactional("securityTransactionManager")
    public AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String refreshTokenValue = request.tokenRefresh();

        // Ищем сессию по токену. Она должна быть ACTIVE.
        AuthSession session = authSessionRepository.findByRefreshTokenAndStatus(refreshTokenValue, SessionStatus.STATUS_ACTIVE)
                .orElseThrow(() -> new TokenRefreshException("Active session for this refresh token not found."));

        // Проверяем срок жизни
        if (session.getExpiresAt().isBefore(Instant.now())) {
            sessionArchivingService.archive(session, RevocationReason.REASON_EXPIRED);
            throw new TokenRefreshException("Refresh token has expired.");
        }

        // --- КРАСНАЯ ТРЕВОГА ---
        String currentFingerprint = this.fingerprintUtils.generate(httpRequest);
        if (!Objects.equals(session.getFingerprintHash(), currentFingerprint)) {
            session.setStatus(SessionStatus.STATUS_RED_ALERT);
            authSessionRepository.save(session);
            sessionArchivingService.archive(session, RevocationReason.REASON_RED_ALERT);
            // eventPublisher.publishEvent(...) // Публикуем событие для безопасников в микросервисе путём кафки
            throw new SecurityBreachAttemptException("Fingerprint mismatch. Session compromised.",
                    session.getUserId(), session.getId(), httpRequest.getRemoteAddr());
        }

        // --- Ротация токена ---
        // 1. Архивируем старую сессию
        sessionArchivingService.archive(session, RevocationReason.REASON_TOKEN_ROTATED);
        // 2. Создаем новую
        User user = userRepository.findById(session.getUserId()).orElseThrow();
        AppUserDetails userDetails = new AppUserDetails(user);

        return login(new LoginRequest(userDetails.getEmail(), null), httpRequest);
    }

    @Override
// Эта операция НЕ требует основной транзакции.
// Но для согласованности можно указать "securityTransactionManager".
    @Transactional("securityTransactionManager")
    public void logout(AppUserDetails userDetails) {
        UUID sessionId = userDetails.getSessionId();
        if (sessionId == null) {
            log.warn("Attempt to logout from a session without sessionId for user {}", userDetails.getId());
            return;
        }

        // Находим активную сессию
        authSessionRepository.findById(sessionId)
                .ifPresent(session -> {
                    // Архивируем ее с причиной LOGOUT
                    sessionArchivingService.archive(session, RevocationReason.REASON_USER_LOGOUT);

                    // Добавляем access_token во временный blacklist
                    // Для этого нам нужен сам токен. В UserDetails его нет.
                    // Это решается на уровне фильтра или контроллера, который имеет доступ к токену.
                    // Пока просто вызовем blacklist с sessionId.
                    // TTL должен быть равен остатку жизни access токена.
                    // Duration timeLeft = ...
                    // blacklistService.addToBlacklist(sessionId, timeLeft);
                    log.info("Session {} for user {} has been logged out.", sessionId, userDetails.getId());
                });
    }
}

//package ru.example.account.security.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import ru.example.account.business.entity.Account;
//import ru.example.account.user.entity.EmailData;
//import ru.example.account.user.entity.PhoneData;
//import ru.example.account.security.entity.RefreshToken;
//import ru.example.account.user.entity.User;
//import ru.example.account.business.repository.AccountRepository;
//import ru.example.account.business.repository.EmailDataRepository;
//import ru.example.account.business.repository.PhoneDataRepository;
//import ru.example.account.user.repository.UserRepository;
//import ru.example.account.security.jwt.JwtUtils;
//import ru.example.account.security.service.SecurityService;
//import ru.example.account.shared.exception.exceptions.RefreshTokenException;
//import ru.example.account.security.model.request.LoginRequest;
//import ru.example.account.security.model.request.RefreshTokenRequest;
//import ru.example.account.security.model.request.UserCredentialsRegisterRequestDto;
//import ru.example.account.security.model.response.AuthResponse;
//import ru.example.account.security.model.response.RefreshTokenResponse;
//import ru.example.account.security.model.response.UserCredentialsResponseDto;
//import java.math.BigDecimal;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SecurityServiceImpl implements SecurityService {
//
//
//    private final AuthenticationManager authenticationManager;
//
//    private final AccountRepository accountRepository;
//
//    private final EmailDataRepository emailDataRepository;
//
//    private final PhoneDataRepository phoneDataRepository;
//
//    private final JwtUtils jwtUtils;
//
//    private final RefreshTokenServiceImpl refreshTokenService;
//
//    private final UserRepository userRepository;
//
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    @Transactional
//    public UserCredentialsResponseDto register(UserCredentialsRegisterRequestDto requestDto) {
//
//        Account account = new Account();
//        account.setBalance(new BigDecimal("10.00"));
//     //   account.setInitialBalance(new BigDecimal("10.00"));
//
//        User user = new User();
//        user.setUsername(requestDto.username());
//        user.setPassword(passwordEncoder.encode(requestDto.password()));
//        user.setDateOfBirth(requestDto.birthDate());
//        user.setRoles(new HashSet<>(requestDto.roles()));
//        user.setUserAccount(account);
//
//        EmailData emailData = new EmailData();
//        emailData.setEmail(requestDto.email());
//        emailData.setUser(user);
//
//        PhoneData phoneData = new PhoneData();
//        phoneData.setPhone(requestDto.phoneNumber());
//        phoneData.setUser(user);
//
//        user.setUserEmails(Set.of(emailData));
//        user.setUserPhones(Set.of(phoneData));
//
//        user = userRepository.save(user);
//
//        return new UserCredentialsResponseDto(
//                user.getUserEmails().stream()
//                        .map(EmailData::getEmail)
//                        .collect(Collectors.toSet())
//        );
//    }
//
//    @Transactional()
//    public AuthResponse authenticationUser(LoginRequest loginRequest) {
//
//        Authentication authentication = authenticationManager
//                .authenticate(new UsernamePasswordAuthenticationToken(
//                        loginRequest.email(),
//                        loginRequest.password()
//                ));
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
////        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
////                userDetails.getUser().getId()
////        );
////new AuthResponse(
////                userDetails.getUser().getId(),
////              //  jwtUtils.generateTokenFromUsername(userDetails.getEmail(), userDetails.getUser().getId()),
////                refreshToken.getTokenRefresh(),
////                userDetails.getUsername(),
////                userDetails.getAuthorities()
////                        .stream()
////                        .map(GrantedAuthority::getAuthority)
////                        .toList());
////    }
//
//        return null;
//    }
//
//    @Transactional()
//    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
//
//        return refreshTokenService.getByRefreshToken(request.tokenRefresh())
//                .map(refreshTokenService::checkRefreshToken)
//                .map(RefreshToken::getUserId)
//                .map(userId -> {
//                    User user = userRepository.findById(userId).orElseThrow(() ->
//                            new RefreshTokenException("User not found with userId: " + userId));
//                    refreshTokenService.deleteByUserId(userId);
//                 //   String token = jwtUtils.generateTokenFromUsername(user.getUsername(), userId);
////                    return new RefreshTokenResponse(token,
////                            refreshTokenService.createRefreshToken(user.getId()).getTokenRefresh());
////                })
////                .orElseThrow(() -> new RefreshTokenException("Invalid token"));
//                    return null});
//    }
//
//
//    @Transactional()
//    public void logout() {
//
//        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (currentPrincipal instanceof AppUserDetails userDetails) {
//            refreshTokenService.deleteByUserId(userDetails.getId());
//        }
//    }
//}
