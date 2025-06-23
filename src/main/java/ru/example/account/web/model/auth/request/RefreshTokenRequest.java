package ru.example.account.web.model.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(@NotBlank @NotNull String tokenRefresh) {
}
