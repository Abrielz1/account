package ru.example.account.security.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.UUID;

public record ClientEmailConfirmationAttemptDto(

        @UUID
        java.util.UUID id,

        @NotBlank
        @Email
        String userEmailToConfirm,

        @NotBlank
        String link
) {
}
