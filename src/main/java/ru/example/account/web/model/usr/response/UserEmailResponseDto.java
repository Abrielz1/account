package ru.example.account.web.model.usr.response;

import java.util.Set;

public record UserEmailResponseDto(Set<String> emails) {
}
