package ru.example.account.security.service.facade;

import jakarta.servlet.http.HttpServletRequest;
import ru.example.account.security.dto.in.ClientEmailConfirmationAttemptDto;
import ru.example.account.security.dto.in.ClientRegisterRequestDto;
import ru.example.account.security.dto.out.ClientRegistrationRequestedResponceDto;
import ru.example.account.security.dto.out.SuccessfullyEmailConfirmationDTO;

public interface ClientRegistrationService {

    ClientRegistrationRequestedResponceDto register(ClientRegisterRequestDto request, HttpServletRequest httpRequest);

    SuccessfullyEmailConfirmationDTO emailConfirmation(ClientEmailConfirmationAttemptDto request, HttpServletRequest httpRequest);
}
