package ru.example.account.web.model.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank
                           @Size(min = 8, max = 32, message = "Minimum 8 chars, Maximum 32")
                           @Email
                           @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                                   flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid email format")
                           String email,

                           @NotBlank
                           @Size(min = 8, max = 32, message = "Minimum 8 chars and Maximum 32 chars")
                           String password) {

}
