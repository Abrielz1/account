package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.entity.SessionAuditLog;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.ActiveSessionCacheRepository;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.SessionAuditLogRepository;
import ru.example.account.security.service.FingerprintService;
import ru.example.account.security.service.IdGenerationService;
import ru.example.account.security.service.SessionPersistenceService;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionPersistenceServiceImpl implements SessionPersistenceService {


    private final AuthSessionRepository authSessionRepository;

    private final ActiveSessionCacheRepository cacheRepository;

    private final SessionAuditLogRepository sessionAuditLogRepository;

    private final FingerprintService fingerprintService;

    private final JwtUtils jwtUtils;

    private final IdGenerationService idGenerationService;

    @Value("${app.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
    @Transactional(value = "securityTransactionManager")
    public AuthSession createAndSaveSession(AppUserDetails currentUser,
                                            String fingerprint,
                                            String ipAddress,
                                            String userAgent) {

        Instant currentTime = Instant.now();

        // --- Шаг 1. Делегируем генерацию уникальных ID
        final UUID newSessionId = this.idGenerationService.generateSessionId();
        // --- Шаг 1: Генерация refreshToken ---
        final String refreshToken = this.idGenerationService.generateUniqueTokenId();
        // --- Шаг 2: Генерация accessToken ---
        final String accessToken = this.jwtUtils.generateAccessToken(currentUser, newSessionId, fingerprint);

        AuthSession session =  AuthSession
                .builder()
                .id(newSessionId)
                .userId(currentUser.getId())
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .createdAt(currentTime)
                .expiresAt(currentTime.plus(refreshTokenExpiration))
                .status(SessionStatus.STATUS_ACTIVE)
                .fingerprint(fingerprint)
                .fingerprintHash(this.jwtUtils.createFingerprintHash(fingerprint)) //
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        log.info("Persisting new AuthSession {} for user {}", newSessionId, currentUser.getId());
        return  authSessionRepository.save(session);
    }

    @Override
    public ActiveSessionCache createAndSaveActiveSessionCache(AuthSession session, AppUserDetails currentUser) {

        String roles = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        ActiveSessionCache redisSession = ActiveSessionCache.builder()
                .fingerprintHash(session.getFingerprintHash())
                .userId(session.getUserId())
                .sessionId(session.getId())
                .accessToken(session.getAccessToken())
                .refreshToken(session.getRefreshToken())
                .roles(roles)
                .ttl(refreshTokenExpiration)
                .build();

        return cacheRepository.save(redisSession);
    }

    @Override
    @Transactional(value = "securityTransactionManager")
    public void createAndSaveAuditLog(AuthSession session) {
        SessionAuditLog newSessionAuditLog = new SessionAuditLog();
        newSessionAuditLog.setSessionId(session.getId());
        newSessionAuditLog.setCreatedAt(session.getCreatedAt());
        newSessionAuditLog.setUserAgent(session.getUserAgent());
        newSessionAuditLog.setIpAddress(session.getIpAddress());
        newSessionAuditLog.setFingerprintHash(session.getFingerprint());

        log.info("starting saving session log");
        sessionAuditLogRepository.save(newSessionAuditLog);
        log.info("session log saved successfully");
    }

    @Override
    public void saveFingerPrint(String fingerprint, String ipAddress, String userAgent, ZonedDateTime lastSeenAt, Long userId) {
        fingerprintService.save(fingerprint, ipAddress, userAgent, lastSeenAt, userId);
    }
}
