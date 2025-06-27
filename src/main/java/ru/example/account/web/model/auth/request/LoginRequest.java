package ru.example.account.web.model.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @Schema(description = "User's email", example = "user@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        @Size(max = 254, message = "Email length must be less than 254 characters")
        String email,

        @Schema(description = "User's password", example = "Password123!")
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
        String password
) {
}