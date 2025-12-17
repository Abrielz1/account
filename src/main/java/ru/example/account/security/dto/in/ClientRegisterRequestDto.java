package ru.example.account.security.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.example.account.shared.annotation.MinAge;
import ru.example.account.shared.marker.OnCreate;
import java.time.LocalDate;

public record ClientRegisterRequestDto(
        @NotBlank
        @Email(message = "supplied email must me valid",groups = OnCreate.class)
        String email,

        @NotBlank
        @Pattern(regexp = "^\\+7\\s?\\d{3}\\s?\\d{3}-\\d{2}-\\d{2}$",
                 message = "Phone number is unacceptable, Phone number must match format: +7 XXX XXX-XX-XX",
                groups = OnCreate.class)
        String phone,

        @Size(min = 8, max = 32, message = "username must not be shorter than 8 chars and must not be exceeded 32 chars length")
        @NotBlank(message = "supplied username must not be empty oe null")
        String userName,

        @NotNull(message = "Date of birth is required")
        @MinAge(message = "client must be 18 or over to continue registration", groups = OnCreate.class)
        LocalDate dateOfBirth,

        @NotBlank(message = "supplied secret question must not be empty or null")
        String secretQuestion0,

        @NotBlank(message = "supplied secret question must not be empty or null")
        String secretQuestion1,

        @NotBlank(message = "supplied secret question must not be empty or null")
        String secretQuestion2
) {
}
