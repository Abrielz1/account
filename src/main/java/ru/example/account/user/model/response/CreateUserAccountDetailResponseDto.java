package ru.example.account.user.model.response;

import java.util.List;

public record CreateUserAccountDetailResponseDto(Long id,
                                                 List<String> emails,
                                                 List<String> phones) {
}
