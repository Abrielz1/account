package ru.example.account.web.model.usr.response;

import java.util.Set;

public record CreateUserAccountDetailResponseDto(Long id,
                                                 Set<String> emails,
                                                 Set<String> phones) {
}
