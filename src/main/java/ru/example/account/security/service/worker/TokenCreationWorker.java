package ru.example.account.security.service.worker;

import ru.example.account.security.dto.in.ClientRegisterRequestDto;

public interface TokenCreationWorker {

    String createToken(ClientRegisterRequestDto request);
}
