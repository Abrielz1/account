package ru.example.account.user.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.example.account.security.model.request.UserRegisterRequestDto;
import ru.example.account.security.service.MailSendService;
import ru.example.account.security.service.TimezoneService;
import ru.example.account.shared.util.LinkGenerator;
import ru.example.account.user.entity.Client;
import ru.example.account.user.model.response.ActivationClientAccountRequest;
import ru.example.account.user.model.response.CreateUserAccountDetailResponseDto;
import ru.example.account.user.repository.ClientRepository;
import ru.example.account.user.service.UserProcessor;
import ru.example.account.user.service.ClientService;
import ru.example.account.shared.exception.exceptions.AlreadyExistsException;
import java.time.ZonedDateTime;
import static ru.example.account.shared.mapper.ClientMapper.toAuthResponse;

@Slf4j
@Service
@CacheConfig(cacheNames = "clients")
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final TimezoneService timezoneService;

    private final UserProcessor userProcessor;

    private final ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder;

    private final MailSendService mailSendService;

    private final LinkGenerator linkGenerator;

    @Override
    @Transactional("businessTransactionManager")
    public CreateUserAccountDetailResponseDto registerNewUser(UserRegisterRequestDto request,
                                                              HttpServletRequest httpRequest) {

        if (!StringUtils.hasText(request.phoneNumber()) || !StringUtils.hasText(request.email())) {
            throw new  IllegalArgumentException("Client must send valid data!");
        }

        if (this.userProcessor.isFreeUsername(request.username())) {
            throw new AlreadyExistsException("Username " + request.username() + " is already taken.");
        }

        if (this.userProcessor.isFreeEmail(request.email())) {
            throw new AlreadyExistsException("Email " + request.email() + " is already taken.");
        }

        if (this.userProcessor.isFreePhone(request.phoneNumber())) {
            throw new AlreadyExistsException("Phone " + request.phoneNumber() + " is already taken.");
        }

        Client newClient = new Client();

        newClient.setFieldsClient(request);

        if (request.invitedBy() != null) {

            newClient.setInvitedBy(this.userProcessor.getReferrer(request.invitedBy())
                    .orElse(null));

            if (newClient.getInvitedBy() == null) {
                log.warn("Referrer with ID {} not found during registration of user {}. Proceeding without referrer.",
                        request.invitedBy(), request.username());
            }
        }

        newClient.setPassword(passwordEncoder.encode(request.password()));

        newClient.addEmail(request.email());
        newClient.addPhone(request.phoneNumber());

        newClient.setRegistrationDateTime(ZonedDateTime.now(timezoneService.getZoneIdFromRequest(httpRequest)));

        clientRepository.save(newClient);

        ActivationClientAccountRequest activationClientAccountRequest = new ActivationClientAccountRequest(request.email(),
                this.linkGenerator.generateActivationLink(request.email(), newClient));

        mailSendService.sendActivationMail(activationClientAccountRequest);

        return toAuthResponse(newClient);
    }
}







