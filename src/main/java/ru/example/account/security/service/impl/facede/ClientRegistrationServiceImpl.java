package ru.example.account.security.service.impl.facede;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.exchange.dto.SendEmailEvent;
import ru.example.account.security.dto.in.ClientRegisterRequestDto;
import ru.example.account.security.dto.out.ClientRegistrationRequestedResponceDto;
import ru.example.account.security.entity.RegistrationRequest;
import ru.example.account.security.entity.enums.RegistrationStatus;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.ClientRegistrationRepository;
import ru.example.account.security.service.facade.ClientRegistrationService;
import ru.example.account.security.service.worker.FingerprintService;
import ru.example.account.security.service.worker.TokenCreationWorker;
import ru.example.account.shared.util.String2CRConverter;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final TokenCreationWorker tokenCreationWorker;

    private final String2CRConverter string2CRConverter;

    private final FingerprintService fingerprintService;

    private final JwtUtils jwtUtils;

    private final ClientRegistrationRepository clientRegistrationRepository;

    private static final String LINK = ""; // url для подтверждения реги

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public ClientRegistrationRequestedResponceDto register(ClientRegisterRequestDto request, HttpServletRequest httpRequest) {

        String token = this.tokenCreationWorker.createToken(request);
        Instant currentTime = Instant.now();

        RegistrationRequest newClientRequest = RegistrationRequest.builder()
                .id(UUID.randomUUID())
                .emailHash(this.string2CRConverter.convertIntoCRC(request.email()))
                .passwordHash(this.string2CRConverter.convertIntoCRC(request.password()))
                .phoneHash(this.string2CRConverter.convertIntoCRC(request.phone()))
                .usernameHash(this.string2CRConverter.convertIntoCRC(request.userName()))
                .verificationToken(token)
                .expiresAt(currentTime.plus(Duration.ofHours(24)))
                .createdAt(currentTime)
                .fingerprintHash(this.jwtUtils.createFingerprintHash(this.fingerprintService.generateUsersFingerprint(httpRequest)))
                .ipAddressHash(this.string2CRConverter.convertIntoCRC(httpRequest.getRemoteAddr()))
                .ipAddress(httpRequest.getRemoteAddr())
                .registrationStatus(RegistrationStatus.CREATED)
                .build();

        String emailBody = "Ваш код: " + LINK + "_" + token;

        applicationEventPublisher.publishEvent(new SendEmailEvent(request.email(), "You're registration is not yet complete? please proceed this given link", emailBody));

        newClientRequest.setIsEmailSent(true);

        this.clientRegistrationRepository.save(newClientRequest);

        return new ClientRegistrationRequestedResponceDto("""
                your registration pending email verification please proceed into your email account and follow given link
                 """);
    }
}
