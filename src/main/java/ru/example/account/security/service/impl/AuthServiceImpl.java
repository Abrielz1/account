package ru.example.account.security.service.impl;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.example.account.security.configuration.RedisKeysProperties;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.entity.TrustedFingerprint;
import ru.example.account.security.entity.UserFingerprintProfile;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.UserFingerprintProfileRepository;
import ru.example.account.security.service.AuthService;
import ru.example.account.security.service.FingerprintService;
import ru.example.account.security.service.HttpUtilsService;
import ru.example.account.security.service.SessionRevocationService;
import ru.example.account.security.service.SessionServiceManager;
import ru.example.account.security.service.TimezoneService;
import ru.example.account.security.service.UserService;
import ru.example.account.shared.exception.exceptions.BadRequestException;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;
import ru.example.account.shared.util.SecurityContextValidator;
import java.security.SecureRandom;
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

    private final StringRedisTemplate redisTemplate;

    private final RedisKeysProperties redisKeys;

    private final UserFingerprintProfileRepository profileRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final String HEADER = "User-Agent";

    private static final String RED_ALERT_MESSAGE = "RED ALERT! A HACKER TRIES BREACH SYSTEM";

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

       final AppUserDetails currentUser = (AppUserDetails) authentication.getPrincipal();

        log.info("User {} successfully authenticated.", currentUser.getUsername());

       final String fingerprint = this.fingerprintService.generateUsersFingerprint(httpRequest);
       final String ipAddress = this.httpUtilsService.getClientIpAddress(httpRequest);
       final String userAgent = httpRequest.getHeader(HEADER);
       final ZonedDateTime lastSeenAt = ZonedDateTime.now();

        // ВАЛИДИРУЕМ ВЕСЬ КОНТЕКСТ СРАЗУ!
        this.contextValidator.validateAtLogin(currentUser, fingerprint, ipAddress); // todo
        this.userService.updateLastLoginAsync(currentUser.getId(), this.timezoneService.getZoneIdFromRequest(httpRequest));

        return this.sessionManager.createSession(
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

       final Claims claimsFromToken = this.jwtUtils.getAllClaimsFromToken(request.refreshToken());

        log.info("Token refresh process started.");
        AppUserDetails currentUser = (AppUserDetails) userDetailsService.loadUserByUsername(claimsFromToken.getIssuer());

        String token;

        if (request.accessesToken().startsWith("Bearer")) {
            token = request.accessesToken().substring(7);
        } else {
            token = request.accessesToken();
        }

       final Boolean refreshTokenIsExists = this.authSessionRepository.existsByRefreshToken(request.refreshToken());
       final boolean accessTokenIsExists = this.jwtUtils.isTokenValid(token);

        if (Boolean.TRUE.equals(!refreshTokenIsExists) && Boolean.TRUE.equals(!accessTokenIsExists)) {
            log.trace(RED_ALERT_MESSAGE);
            ///Revoke all session
            this.sessionRevocationService.revokeAllSessionsForUser(currentUser.getId(),
                    SessionStatus.STATUS_COMPROMISED,
                    RevocationReason.REASON_RED_ALERT);
            throw new SecurityBreachAttemptException(RED_ALERT_MESSAGE);
        }

       final String ipAddress = this.httpUtilsService.getClientIpAddress(httpRequest);
       final String userAgent = httpRequest.getHeader(HEADER);
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

    @Override
    @Transactional("businessTransactionManager")
    public void trustDevice(Long userId, String fingerprint, HttpServletRequest request) {
       final String fingerprintHash = jwtUtils.createFingerprintHash(fingerprint);
       final UserFingerprintProfile profile = profileRepository.findById(userId).orElseGet(() -> createNewProfile(userId));
        profile.setLastUpdatedAt(ZonedDateTime.now());
        TrustedFingerprint trustedFp = findOrCreateTrustedFingerprint(profile, fingerprintHash);
        trustedFp.setLastSeenAt(ZonedDateTime.now());
        trustedFp.setIpAddress(request.getRemoteAddr());
        trustedFp.setUserAgent(request.getHeader(HEADER));
        trustedFp.setDeviceName("Unknown Device");
        trustedFp.setTrusted(true);
        this.profileRepository.save(profile);
        log.info("Fingerprint for user {} has been trusted/updated in Postgres.", userId);
        warmUpWhitelistCache(userId, fingerprintHash);
    }

    @Override
    public String generateAndCacheVerificationCode(Long userId, String fingerprint) {
       final String code = String.format("%06d", secureRandom.nextInt(999999));
       final String key = buildVerificationRedisKey(userId, fingerprint);
        this.redisTemplate.opsForValue().set(key, code, redisKeys.getTtl().getDeviceVerificationCode());
        return code;
    }

    @Override
    public boolean verifyCode(Long userId, String fingerprint, String code) {
        String key = buildVerificationRedisKey(userId, fingerprint);
        String storedCode = redisTemplate.opsForValue().get(key);
        if (code != null && code.equals(storedCode)) {
            this.redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private TrustedFingerprint findOrCreateTrustedFingerprint(UserFingerprintProfile profile, String fingerprintHash) {
        return profile.getTrustedFingerprints().stream()
                .filter(fp -> fp.getFingerprint().equals(fingerprintHash))
                .findFirst()
                .orElseGet(() -> {
                    TrustedFingerprint newFp = new TrustedFingerprint();
                    newFp.setFingerprint(fingerprintHash);
                    newFp.setFirstSeenAt(ZonedDateTime.now());
                    profile.addFingerprint(newFp);
                    return newFp;
                });
    }

    private String buildVerificationRedisKey(Long userId, String fingerprint) {
       final String fingerprintHash = jwtUtils.createFingerprintHash(fingerprint);
       String keyFormat = redisKeys.getKeys().getVerification().getDeviceCodeFormat();
        return keyFormat.replace("{userId}", String.valueOf(userId)).replace("{fingerprint}", fingerprintHash);
    }

    private void warmUpWhitelistCache(Long userId, String fingerprintHash) {
        try {
           final String key = this.buildWhitelistRedisKey(fingerprintHash);
            this.redisTemplate.opsForValue().set(key, String.valueOf(userId), redisKeys.getTtl().getTrustedFingerprint());
        } catch (RedisConnectionFailureException e) {
            log.error("REDIS IS STILL DOWN! Could not warm up whitelist cache for user {}.", userId, e);
        }
    }

    private String buildWhitelistRedisKey(String fingerprintHash) {
        return this.redisKeys.getKeys().getWhitelist().getFingerprintKeyFormat().replace("{fingerprint}", fingerprintHash);
    }

    private UserFingerprintProfile createNewProfile(Long userId) {
        UserFingerprintProfile newProfile = new UserFingerprintProfile();
        newProfile.setUserId(userId);
        newProfile.setCreatedAt(ZonedDateTime.now());
        return newProfile;
    }
}