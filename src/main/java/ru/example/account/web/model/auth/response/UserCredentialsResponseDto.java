package ru.example.account.web.model.auth.response;

import java.util.Set;

public record UserCredentialsResponseDto(Set<String> email, String password) {
}
