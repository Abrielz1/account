package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.example.account.security.service.MailSendService;
import org.springframework.mail.javamail.JavaMailSender;
import ru.example.account.user.model.response.ActivationClientAccountRequest;

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

}
