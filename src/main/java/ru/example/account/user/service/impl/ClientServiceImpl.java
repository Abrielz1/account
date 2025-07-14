package ru.example.account.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.example.account.security.model.request.UserRegisterRequestDto;
import ru.example.account.user.entity.Client;
import ru.example.account.user.entity.RoleType;
import ru.example.account.user.model.response.CreateUserAccountDetailResponseDto;
import ru.example.account.user.repository.ClientRepository;
import ru.example.account.user.service.UserProcessor;
import ru.example.account.user.service.ClientService;
import ru.example.account.shared.exception.exceptions.AlreadyExistsException;
import static ru.example.account.shared.mapper.ClientMapper.toAuthResponse;

@Slf4j
@Service
@CacheConfig(cacheNames = "clients")
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserProcessor userProcessor;

    private final ClientRepository castomerRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional("businessTransactionManager")
    public CreateUserAccountDetailResponseDto registerNewUser(UserRegisterRequestDto request) {

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

        newClient.setPassword(passwordEncoder.encode(request.password()));

        newClient.addRole(RoleType.ROLE_CLIENT);
        newClient.addEmail(request.email());
        newClient.addPhone(request.phoneNumber());

        castomerRepository.save(newClient);

        return toAuthResponse(newClient);
    }
}











