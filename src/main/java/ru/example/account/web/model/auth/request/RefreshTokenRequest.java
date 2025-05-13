package ru.example.account.web.model.auth.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(@NotBlank String tokenRefresh) {
}
