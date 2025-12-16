package ru.example.account.security.service.facade;

import ru.example.account.security.entity.enums.RevocationReason;
import ru.example.account.security.principal.AppUserDetails;
import ru.example.account.user.model.response.ActivationClientAccountRequest;
import java.time.ZonedDateTime;

public interface MailSendService {

    void resetClientPassword();

    void sendActivationMail(ActivationClientAccountRequest activationClientAccountRequest);

    void sendAlertMail(String fingerprint, String ipAddress, String userAgent, ZonedDateTime lastSeenAt, Long id);

    void sendRedAlertNotification(Long userId, String fingerprint, String ipAddress, String userAgent, AppUserDetails currentUser, RevocationReason revocationReason);

    void sendRedAlertNotification(Long userId, String fingerprint, String ipAddress, String userAgent, RevocationReason revocationReason);

    void sendReplayAttackNotification(String refreshToken, String accessesToken, String fingerprint, String ipAddress, String userAgent, AppUserDetails currentUser);
}
