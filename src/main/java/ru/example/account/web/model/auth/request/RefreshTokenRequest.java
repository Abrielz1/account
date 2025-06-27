package ru.example.account.web.model.auth.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenRequest(
        @Schema(description = "Refresh token string")
        @NotBlank(message = "Refresh token cannot be blank")
        String tokenRefresh
) {
}