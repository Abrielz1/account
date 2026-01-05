package ru.example.account.security.service.impl.facede;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.exchange.dto.SendEmailEvent;
import ru.example.account.security.dto.in.ClientEmailConfirmationAttemptDto;
import ru.example.account.security.dto.in.ClientRegisterRequestDto;
import ru.example.account.security.dto.out.ClientRegistrationRequestedResponceDto;
import ru.example.account.security.dto.out.SuccessfullyEmailConfirmationDTO;
import ru.example.account.security.entity.RegistrationRequest;
import ru.example.account.security.entity.enums.RegistrationStatus;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.repository.ClientRegistrationRepository;
import ru.example.account.security.service.facade.ClientRegistrationService;
import ru.example.account.security.service.worker.FingerprintService;
import ru.example.account.security.service.worker.TokenCreationWorker;
import ru.example.account.shared.exception.exceptions.BadRequestException;
import ru.example.account.shared.exception.exceptions.UserNotFoundException;
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

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final String LINK = ""; // url для подтверждения реги

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public ClientRegistrationRequestedResponceDto register(ClientRegisterRequestDto request, HttpServletRequest httpRequest) {

        String token = this.tokenCreationWorker.createToken(request);
        Instant currentTime = Instant.now();
        UUID userTemporaryId = UUID.randomUUID();

        RegistrationRequest newClientRequest = RegistrationRequest.builder()
                .id(userTemporaryId)
                .emailHash(this.string2CRConverter.convertIntoCRC(request.email()))
                .passwordHash(this.bCryptPasswordEncoder.encode(request.password()))
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

        String emailBody = "Ваш код: " + LINK + "_" + token + " ваш временный id" + userTemporaryId;

        applicationEventPublisher.publishEvent(new SendEmailEvent(request.email(), "You're registration is not yet complete? please proceed this given link", emailBody));

        newClientRequest.setIsEmailSent(true);

        this.clientRegistrationRepository.save(newClientRequest);

        return new ClientRegistrationRequestedResponceDto("""
                your registration pending email verification please proceed into your email account and follow given link
                 """);
    }

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public SuccessfullyEmailConfirmationDTO emailConfirmation(ClientEmailConfirmationAttemptDto request,
                                                              HttpServletRequest httpRequest) {

        String emailHash = this.string2CRConverter.convertIntoCRC(request.userEmailToConfirm());
        String[] tokenArray = request.link().split("_");

        RegistrationRequest unconfirmedClient =
                this.clientRegistrationRepository.findByIdAndAndEmailHashAndVerificationToken(request.id(), emailHash, tokenArray[2])
                        .orElseThrow(() -> {
                            log.warn("[WARN] client was not fond");
                            return new UserNotFoundException("client was not fond");
                        });

        if (unconfirmedClient.getExpiresAt().toEpochMilli() < Instant.now().toEpochMilli()) {
            unconfirmedClient.setIsExpired(true);
            log.warn("[WARN] client tries to confirm email but it too late");
            // applicationEventPublisher.publishEvent(); // client wes moved it to expired group client must contact Bank security
            // applicationEventPublisher.publishEvent(); // Bank security was informed about incident
        }

        if (unconfirmedClient.getIsExpired()) {
            log.warn("[WARN]");
            // applicationEventPublisher.publishEvent(); // client wes moved it to expired group client must contact Bank security
            // applicationEventPublisher.publishEvent(); // Bank security was informed about incident
            throw new BadRequestException("");
        }

        if (unconfirmedClient.getIsBlocked()) {
            log.warn("[WARN]");
            // applicationEventPublisher.publishEvent(); // Bank security was informed about incident
            throw new BadRequestException("");
        }

        unconfirmedClient.setIsEmailVerified(true);
       // applicationEventPublisher.publishEvent();

        return new SuccessfullyEmailConfirmationDTO("Yuure email was confirmed" +
                " please proceed through supplied link to complete registration", "link " + request.userEmailToConfirm());
    }

    @Bean
    public BCryptPasswordEncoder create() {
        return new BCryptPasswordEncoder();
    }
 }
