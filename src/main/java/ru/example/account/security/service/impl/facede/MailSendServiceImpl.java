package ru.example.account.security.service.impl.facede;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.principal.AppUserDetails;
import ru.example.account.security.service.facade.MailSendService;
import org.springframework.mail.javamail.JavaMailSender;
import ru.example.account.user.model.response.ActivationClientAccountRequest;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSendServiceImpl implements MailSendService {

    private final JavaMailSender javaMailSender;

    @Value("${EMAIL_FROM}")
    private String from;

    @Override
    public void resetClientPassword() {


    }

    @Override
    public void sendActivationMail(ActivationClientAccountRequest activationClientAccountRequest) {


    }

    @Override
    public void sendAlertMail(String fingerprint, String ipAddress, String userAgent, ZonedDateTime lastSeenAt, Long id) {

    }

    @Override
    public void sendRedAlertNotification(Long userId, String fingerprint, String ipAddress, String userAgent, AppUserDetails currentUser, RevocationReason revocationReason) {

    }

    @Override
    public void sendRedAlertNotification(Long userId, String fingerprint, String ipAddress, String userAgent, RevocationReason revocationReason) {

    }

    @Override
    public void sendReplayAttackNotification(String refreshToken, String accessesToken, String fingerprint, String ipAddress, String userAgent, AppUserDetails currentUser) {

    }

}
