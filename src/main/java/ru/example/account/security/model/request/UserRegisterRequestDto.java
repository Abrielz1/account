package ru.example.account.security.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.example.account.user.entity.RoleType;
import java.time.LocalDate;
import java.util.Set;

public record UserRegisterRequestDto(

        @Schema(description = "User's email", example = "newuser@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        @Size(max = 254)
        String email,

        @Schema(description = "User's password", example = "Password123!")
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
        String password,

        @Schema(description = "Username", example = "new_user_login")
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Schema(description = "User's phone number", example = "+79991234567")
        @NotBlank(message = "Phone number cannot be blank")
        @Pattern(regexp = "^\\+7\\d{10}$", message = "Phone number must be in format +7XXXXXXXXXX")
        String phoneNumber,

        @Schema(description = "User's date of birth", example = "1990-01-15")
        @NotNull(message = "Date of birth cannot be null")
        @Past(message = "Date of birth must be in the past")
        LocalDate birthDate,

        @Schema(description = "User's referal link", example = "http://bank/user/1")
        @Size(min = 100, max = 250, message = "URL must be between 100 and 250 characters")
        String registrationSource,

        @NotNull
        @Positive
        @Min(value = 1L)
        Long invitedBy,

        @Schema(description = "User's account", example = "my money for vacation")
        @NotBlank
        @NotEmpty
        @Size(min = 12, max = 255, message = "Accoun namet must be between 12 and 255 characters")
        String accountName
) {
}