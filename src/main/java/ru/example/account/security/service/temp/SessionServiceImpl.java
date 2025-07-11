package ru.example.account.security.service.temp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RefreshTokenRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;
import ru.example.account.security.repository.SessionAuditLogRepository;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    // Репозитории для 3х баз
    private final RefreshTokenRepository refreshTokenRedisRepo; // Для Redis

    private final AuthSessionRepository authSessionPostgresRepo; // Для Postgres

    private final SessionAuditLogRepository sessionAuditLogRepository; // Для Postgres

    private final RevokedTokenArchiveRepository archivePostgresRepo; // для Postgres

    @Value("${app.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Value("${app.jwt.ttl}")
    private Long timeToLive;

//    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public RefreshToken createSessionAndToken(Long userId, String fingerprintHash, String ipAddress, String userAgent) {
//
//        UUID sessionId = UUID.randomUUID();
//
//        final int MAX_COUNT = 4;
//
//        int CURRENT_COUNT = 0;
//
//        while (CURRENT_COUNT < MAX_COUNT) {
//
//            if (authSessionPostgresRepo.checkSessionIdAuditLog(sessionId.toString()) && sessionAuditLogRepository.checkSessionIdAuthSession(sessionId.toString())){
//                sessionId = UUID.randomUUID();
//                ++CURRENT_COUNT;
//            } else {
//                break;
//            }
//
//            if (CURRENT_COUNT >= MAX_COUNT) {
//                log.error("CRITICAL: Failed to generate a unique session ID after {} attempts.", MAX_COUNT);
//                throw new IllegalStateException("Cannot generate a unique session ID.");
//            }
//        }
//
//        UUID refreshToken = UUID.randomUUID();
//        if (refreshTokenRedisRepo.existsByToken(refreshToken.toString())) {
//
//           refreshToken = UUID.randomUUID();
//        }
//
//        AuthSession newSession = AuthSession.builder()
//                .id(sessionId)
//                .userId(userId)
//// Генерируем уникальную, случайную строку для самого refresh токена
//                .refreshToken(refreshToken.toString())
//                .status(SessionStatus.STATUS_ACTIVE)
//                .fingerprintHash(fingerprintHash)
//                .ipAddress(ipAddress)
//                .userAgent(userAgent)
//                .createdAt(Instant.now())
//                .expiresAt(Instant.now().plus(timeToLive, ChronoUnit.SECONDS))
//                .build();
//
//        authSessionPostgresRepo.save(newSession);
//
//        RefreshToken newToken = RefreshToken.builder()
//                .sessionId(sessionId) // <-- Связь через sessionId
//                .userId(userId)
//                .token(refreshToken.toString()) // <-- Та же случайная строка
//                .timeToLive(timeToLive)
//                .build();
//
//        refreshTokenRedisRepo.save(newToken);
//
//        return newToken;
//    }
//
//    @Override
//    public RefreshToken findActiveRefreshToken(String token) {
//        return null;
//    }
//
//    @Override
//    public AuthSession findSessionById(UUID sessionId) {
//        return null;
//    }
//
//    @Override
//    public void archiveAndRevoke(AuthSession session, RefreshToken token, RevocationReason reason) {
//
//    }
//
//    @Override
//    public void revokeAllForUser(Long userId, RevocationReason reason) {
//
//    }
}