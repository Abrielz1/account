package ru.example.account.security.model.response;

import java.util.Set;

public record UserCredentialsResponseDto(Set<String> email) {
}
