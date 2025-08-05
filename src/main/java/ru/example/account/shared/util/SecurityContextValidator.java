package ru.example.account.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.BlockedEntityType;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.BannedEntityRepository;
import ru.example.account.security.service.MailSendService;
import ru.example.account.security.service.SessionCommandService;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.TrustedDeviceService;
import ru.example.account.security.service.impl.AppUserDetails;
import ru.example.account.shared.exception.exceptions.AccessDeniedException;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;
import ru.example.account.shared.exception.exceptions.UserNotVerifiedException;
import ru.example.account.user.entity.RoleType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityContextValidator {


    private final SessionQueryService sessionQueryService;

    private final TrustedDeviceService trustedDeviceService;

    private final SessionCommandService sessionCommandService;

    private final JwtUtils jwtUtils;

    private final BannedEntityRepository bannedEntityRepository;

    private final MailSendService mailSendService;

    private static final Set<String> VALID_ROLES = Arrays.stream(RoleType.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    @Transactional(value = "securityTransactionManager", readOnly = true)
    public void validate(AppUserDetails user, String fingerprint, String ipAddress, String userAgent, String accessToken) {

        // --- ПРЕДВАРИТЕЛЬНЫЕ ПРОВЕРКИ ---
        if (!user.isEnabled()) {
            throw new UserNotVerifiedException("User account is not active.");
        }
        if (isPayloadSuspicious(user)) {
            throw new SecurityBreachAttemptException("Invalid user context payload.");
        }

        // --- ШАГ 1 "СНАЧАЛА - ХОЛОДНЫЙ ИСТОЧНИК ПРАВДЫ (POSTGRES)" ---

        Optional<AuthSession> sessionOpt = sessionQueryService.findActiveByAccessToken(accessToken);

        if (sessionOpt.isEmpty()) {
            log.error("CRITICAL SECURITY ALERT: No active session for user {}", user.getId());
            throw new SecurityBreachAttemptException("No active session for the provided token.");
        }

        AuthSession sessionFromDb = sessionOpt.get();
        String hashFromDb = sessionFromDb.getFingerprintHash();

        // --- ШАГ 2 "СВЕРКА ХЭШЕЙ" (Token Binding) ---
        String hashFromRequest = jwtUtils.createFingerprintHash(fingerprint);

        if (!Objects.equals(hashFromDb, hashFromRequest)) {
            log.error("CRITICAL SECURITY ALERT: Token-Device binding failed for user {}.", user.getId());
            log.error("Hacker intrusion!. Red Alert!");

            String reason = String.format(
                    "Security violation for session is %s, and userId is %d",
                    sessionFromDb.getId(), user.getId() );

            log.error("CRITICAL [SECURITY]: {}", reason);

            // ... (протокол "Красная тревога": сдампить всю сессию, отозвать все сессии, отправить алерт) ...
            this.sessionCommandService.archiveAllForUser(sessionFromDb.getUserId(), fingerprint, ipAddress, userAgent, RevocationReason.REASON_RED_ALERT);

            // шлём срочное уведомление об хакерской атаке
            this.mailSendService.sendRedAlertNotification(sessionFromDb.getUserId(), fingerprint, ipAddress, userAgent, RevocationReason.REASON_RED_ALERT);

            throw new SecurityBreachAttemptException("Token-Device binding validation failed!");
        }

        // --- ШАГ 3 "И ТОЛЬКО ПОТОМ ИСКАТЬ ТОКЕНЫ" (в списках) ---

        // 3a. БЕЛЫЙ СПИСОК (Whitelist)
        if (!trustedDeviceService.isDeviceTrusted(user.getId(), fingerprint)) {
            log.warn("CRITICAL: Access from untrusted device for user {}", user.getId());
            throw new SecurityBreachAttemptException("Access from an untrusted device is forbidden.");
        }

        // 3b. "БАНХАММЕР" (проверка по IP/UserID/...)
        if (isBanned(user, fingerprint, ipAddress)) {
            throw new AccessDeniedException("Access denied due to security restrictions (entity banned).");
        }

        log.trace("Security context validated successfully for user {}", user.getId());
    }

    private boolean isBanned(AppUserDetails user, String fingerprint, String ipAddress) {

        boolean result = false;

        if (bannedEntityRepository.isEntityBanned(BlockedEntityType.USER_ID, user.getId())) {
            result = true;
        }

        if (bannedEntityRepository.isIpBanned(BlockedEntityType.IP_ADDRESS, ipAddress)) {
            result = true;
        }

        if (bannedEntityRepository.isFingerprintBanned(BlockedEntityType.FINGERPRINT, fingerprint)) {
            result = true;
        }
        return result;
    }

    private boolean isPayloadSuspicious(AppUserDetails user) {
        if (user.getId() == null || user.getId() <= 0) return true;
        if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) return true;
        List<String> userRoles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return !VALID_ROLES.containsAll(userRoles);
    }
}