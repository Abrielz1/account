package ru.example.account.security.model.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenRequest(
        @Schema(description = "The long-lived Refresh token string")
        @NotBlank(message = "Refresh token cannot be blank")
        String refreshToken,

        @Schema(description = "The short-lived Accesses token string")
        @NotBlank(message = "Accesses token cannot be blank")
        String accessesToken
) {
}