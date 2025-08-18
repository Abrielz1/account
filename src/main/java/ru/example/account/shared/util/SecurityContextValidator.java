package ru.example.account.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.example.account.security.entity.BlockedEntityType;
import ru.example.account.security.repository.BannedEntityRepository;
import ru.example.account.security.principal.AppUserDetails;
import ru.example.account.shared.exception.exceptions.AccessDeniedException;
import ru.example.account.shared.exception.exceptions.SecurityBreachAttemptException;
import ru.example.account.shared.exception.exceptions.UserNotVerifiedException;
import ru.example.account.user.entity.RoleType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityContextValidator {

 //   private final WhitelistService trustedDeviceService;

    private final BannedEntityRepository bannedEntityRepository;

    private static final Set<String> VALID_ROLES = Arrays.stream(RoleType.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    @Transactional(value = "securityTransactionManager", readOnly = true)
    public void validateAtLogin(AppUserDetails user, String fingerprint, String ipAddress) {

        if (user == null) {
            log.error("");
            return;
        }

        if (StringUtils.hasText(fingerprint) && !StringUtils.hasText(ipAddress)) {
            log.error("");
            return;
        }

        log.debug("Performing NEW LOGIN validation for user", user.getId());

        if (this.ipAddresIsBlacked(ipAddress))

        this.validateNewSessionContext(user, fingerprint, ipAddress);

        // Если все "стражники" пройдены, значит, с этим контекстом можно создавать сессию.
        log.info("New login context for user {} successfully validated.", user.getId());
    }

    private boolean ipAddresIsBlacked(String ipAddress) {
        return false;
    }

    @Transactional(value = "securityTransactionManager", readOnly = true)
    public void validateAtRefreshSession(AppUserDetails user, String fingerprint, String ipAddress, String accessToken){

        log.debug("Performing NEW LOGIN validation for user: {}", user.getId());

        this.validateNewSessionContextAtLogin(user, fingerprint, ipAddress, accessToken);

        // Если все "стражники" пройдены, значит, с этим контекстом можно создавать сессию.
        log.info("New login context for user {} successfully validated.", user.getId());
    }

    public void validateNewSessionContextAtLogin(AppUserDetails user, String fingerprint, String ipAddress, String accessToken) {
        // --- ПРОВЕРЯЕМ ТОЛЬКО ТО, ЧТО МОЖЕМ ---
        // Здесь мы НЕ МОЖЕМ проверить Token Binding, т.к. токена еще нет.

        // --- ПРОВЕРКА №1: "ГИГИЕНА" ---
        // Эти проверки не требуют токенов
        if (!user.isEnabled()) {
            throw new UserNotVerifiedException("User account is not active.");
        }

        if (this.isPayloadSuspicious(user)) {
            // Эта проверка важна, т.к. UserDetails мы СОЗДАЛИ сами на основе данных из БД
            throw new SecurityBreachAttemptException("Invalid user context payload constructed post-authentication.");
        }
        // --- ПРОВЕРКА №2: "БАНХАММЕР" ---
        // 2. Проверяем, не забанен ли кто-то из "участников"
        if (this.isBanned(user, fingerprint, ipAddress)) {
            throw new AccessDeniedException("Access attempt from a banned entity.");
        }

        // --- ПРОВЕРКА №3: "БЕЛЫЙ СПИСОК" (Опциональная, но "слоновья") ---
        // Эта проверка НЕ требует токенов
//        if (!this.trustedDeviceService.isDeviceTrusted(user.getId(), accessToken, fingerprint)) {
//            // Бросаем наше "умное" исключение, которое в будущем "запустит" верификацию.
//            throw new DeviceNotVerifiedException(user.getId(), fingerprint);
//        }
    }

    public void validateNewSessionContext(AppUserDetails user, String fingerprint, String ipAddress) {
        // --- ПРОВЕРЯЕМ ТОЛЬКО ТО, ЧТО МОЖЕМ ---
        // Здесь мы НЕ МОЖЕМ проверить Token Binding, т.к. токена еще нет.

        // --- ПРОВЕРКА №1: "ГИГИЕНА" ---
        // Эти проверки не требуют токенов
        if (!user.isEnabled()) {
            throw new UserNotVerifiedException("User account is not active.");
        }

        if (this.isPayloadSuspicious(user)) {
            // Эта проверка важна, т.к. UserDetails мы СОЗДАЛИ сами на основе данных из БД
            throw new SecurityBreachAttemptException("Invalid user context payload constructed post-authentication.");
        }
        // --- ПРОВЕРКА №2: "БАНХАММЕР" ---
        // 2. Проверяем, не забанен ли кто-то из "участников"
        if (this.isBanned(user, fingerprint, ipAddress)) {
            throw new AccessDeniedException("Access attempt from a banned entity.");
        }
    }

    private boolean isBanned(AppUserDetails user, String fingerprint, String ipAddress) {

        if (this.bannedEntityRepository.isEntityBanned(BlockedEntityType.USER_ID, user.getId())) {
            log.warn("Access attempt by BANNED User: {}", user.getId());
            return true;
        }

        if (this.bannedEntityRepository.isIpBanned(BlockedEntityType.IP_ADDRESS, ipAddress)) {
            log.warn("Access attempt from BANNED IP: {}. User: {}", ipAddress, user.getId());
            return true;
        }

        if (this.bannedEntityRepository.isFingerprintBanned(BlockedEntityType.FINGERPRINT, fingerprint)) {
            log.warn("Access attempt from BANNED Fingerprint. User: {}", user.getId());
            return true;
        }

        return false;
    }

    private boolean isPayloadSuspicious(AppUserDetails user) {
        if (user.getId() == null || user.getId() <= 0) return true;
        if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) return true;
        final  List<String> userRoles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return !VALID_ROLES.containsAll(userRoles);
    }
}