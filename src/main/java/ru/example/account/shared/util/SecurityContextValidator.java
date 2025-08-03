package ru.example.account.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import ru.example.account.security.entity.BlockedEntityType;
import ru.example.account.security.repository.BannedEntityRepository;
import ru.example.account.security.repository.TrustedFingerprintRepository;
import ru.example.account.security.service.impl.AppUserDetails;
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

    // --- ТОЛЬКО "ЧИТАЮЩИЕ" ЗАВИСИМОСТИ ---
    private final TrustedFingerprintRepository trustedFingerprintRepository;

    private final BannedEntityRepository bannedEntityRepository;

    private final Set<String> VALID_ROLES = Arrays.stream(RoleType.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    public void validate(AppUserDetails user, String fingerprint, String ipAddress) {

        if (isBanned(user, fingerprint, ipAddress)) {
            // Просто бросаем исключение. Кто-то ВЫШЕ поймает его и решит, что делать.
            throw new AccessDeniedException("Access denied due to security restrictions.");
        }

        if (!user.isEnabled()) {
            throw new UserNotVerifiedException("User account is not active.");
        }

        if (isPayloadSuspicious(user)) {
            log.warn("CRITICAL: Invalid user context payload detected for user {}", user.getId());
            throw new SecurityBreachAttemptException("Invalid user context payload.");
        }

        if (!trustedFingerprintRepository.isFingerprintTrustedForUser(user.getId(), fingerprint)) {
            log.warn("CRITICAL: Access from untrusted device for user {}", user.getId());
            throw new SecurityBreachAttemptException("Access from untrusted device.");
        }
    }

    private boolean isBanned(AppUserDetails user, String fingerprint, String ipAddress) {
        if (bannedEntityRepository.isEntityBanned(BlockedEntityType.USER_ID, user.getId())) {
            log.warn("BANNED user access attempt: {}", user.getId());
            return true;
        }
        if (bannedEntityRepository.isEntityBanned(BlockedEntityType.USER_ID, user.getId())) {
            log.warn("Access attempt from BANNED IP: {}", ipAddress);
            return true;
        }
        if (bannedEntityRepository.isEntityBanned(BlockedEntityType.USER_ID, user.getId())) {
            log.warn("Access attempt from BANNED Fingerprint: {}", fingerprint);
            return true;
        }
        return false;
    }

    private boolean isPayloadSuspicious(AppUserDetails user) {
        if (user.getId() == null || user.getId() <= 0) {
            log.warn("Suspicious payload: User ID is null or non-positive.");
            return true;
        }

        // Проверка №2: У пользователя должны быть какие-то роли
        if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
            log.warn("Suspicious payload: User {} has no authorities (roles).", user.getId());
            return true;
        }

        // Проверка №3: Все роли пользователя должны быть "известны" нашей системе
        List<String> userRoles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (!VALID_ROLES.containsAll(userRoles)) {
            log.warn("Suspicious payload: User {} has unknown roles: {}", user.getId(), userRoles);
            return true;
        }

        // Если все проверки пройдены
        return false;
    }
}
