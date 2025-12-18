package ru.example.account.security.service.impl.facede;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.example.account.exchange.dto.SendEmailEvent;
import ru.example.account.security.dto.in.ClientRegisterRequestDto;
import ru.example.account.security.dto.out.ClientRegistrationRequestedResponceDto;
import ru.example.account.security.service.facade.ClientRegistrationService;
import ru.example.account.security.service.worker.TokenCreationWorker;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final TokenCreationWorker tokenCreationWorker;

    @Override
    public ClientRegistrationRequestedResponceDto register(ClientRegisterRequestDto request, HttpServletRequest httpRequest) {

        String token = this.tokenCreationWorker.createToken(request);

        String emailBody = "Ваш код: " + token;

        applicationEventPublisher.publishEvent(new SendEmailEvent(request.email(), "You're registration is not yet complete? please procced this given link", token));

        return null;
    }
}
