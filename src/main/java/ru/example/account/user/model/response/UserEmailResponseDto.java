package ru.example.account.user.model.response;

import java.util.Set;

public record UserEmailResponseDto(Set<String> emails) {
}
