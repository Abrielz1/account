package ru.example.account.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.example.account.security.entity.TrustedFingerprint;
import ru.example.account.security.entity.UserFingerprintProfile;
import java.time.ZonedDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FingerprintProfileHelper {

    public UserFingerprintProfile createNewProfile(final Long userId) {
        UserFingerprintProfile newProfile = new UserFingerprintProfile();
        newProfile.setUserId(userId);
        ZonedDateTime now = ZonedDateTime.now();
        newProfile.setCreatedAt(now);
        newProfile.setLastUpdatedAt(now);
        return newProfile;
    }

    public TrustedFingerprint findOrCreateTrustedFingerprint(final UserFingerprintProfile profile, final String fingerprint) {
        Optional<TrustedFingerprint> existingFp = profile.getTrustedFingerprints().stream()
                .filter(fp -> fp.getFingerprint().equals(fingerprint))
                .findFirst();

        if (existingFp.isPresent()) {
            return existingFp.get();
        } else {
            TrustedFingerprint newTrustedFingerprint = new TrustedFingerprint();
            newTrustedFingerprint.setTrusted(true);
            newTrustedFingerprint.setDeviceName("Unknown Device");
            newTrustedFingerprint.setFingerprint(fingerprint);
            newTrustedFingerprint.setFirstSeenAt(ZonedDateTime.now());
            profile.addFingerprint(newTrustedFingerprint); // Используем наш "чистый" метод-хелпер

            return newTrustedFingerprint;
        }
    }
}
