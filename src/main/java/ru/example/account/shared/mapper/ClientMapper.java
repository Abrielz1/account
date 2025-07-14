package ru.example.account.shared.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.example.account.user.entity.Client;
import ru.example.account.user.entity.EmailData;
import ru.example.account.user.entity.PhoneData;
import ru.example.account.user.model.response.CreateUserAccountDetailResponseDto;

@Slf4j
public class ClientMapper {

    private ClientMapper() {
        log.error("");
        throw new IllegalStateException();
    }

    public static CreateUserAccountDetailResponseDto toAuthResponse(Client newClient) {

        return new CreateUserAccountDetailResponseDto(
                newClient.getId(),

                newClient.getUserEmails().stream()
                .map(EmailData::getEmail)
                .toList(),

                newClient.getUserPhones().stream()
                .map(PhoneData::getPhone)
                .toList()
        );
    }
}
