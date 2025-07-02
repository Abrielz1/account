package ru.example.account.user.model.response;

import java.util.Set;

public record CreateUserAccountDetailResponseDto(Long id,
                                                 Set<String> emails,
                                                 Set<String> phones) {
}
