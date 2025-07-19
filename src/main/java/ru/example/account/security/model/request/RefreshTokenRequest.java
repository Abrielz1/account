package ru.example.account.security.model.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
        @Schema(description = "The long-lived Refresh token string")
        @NotBlank(message = "Refresh token cannot be blank")
        String refreshToken,

        @Schema(description = "The short-lived Accesses token string")
        @NotBlank(message = "Accesses token cannot be blank")
        String accessesToken,

        @Schema(description = "Username", example = "new_user_login")
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username
) {
}