package ru.example.account.security.service;

import ru.example.account.user.model.response.ActivationClientAccountRequest;

public interface MailSendService {

    void resetClientPassword();

    void sendActivationMail(ActivationClientAccountRequest activationClientAccountRequest);
}
