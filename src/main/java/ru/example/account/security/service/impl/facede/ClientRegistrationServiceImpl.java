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

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ClientRegistrationRequestedResponceDto register(ClientRegisterRequestDto request, HttpServletRequest httpRequest) {

        String token = "";

        String emailBody = "Ваш код: " + token;

        applicationEventPublisher.publishEvent(new SendEmailEvent(request.email, "", token));

        return null;
    }
}
