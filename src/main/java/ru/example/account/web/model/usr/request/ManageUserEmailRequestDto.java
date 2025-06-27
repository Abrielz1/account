package ru.example.account.web.model.usr.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ManageUserEmailRequestDto(
        @Schema(description = "New email address", example = "mynewemail@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        @Size(max = 254, message = "Email length must be less than 254 characters")
        String email
) {
}