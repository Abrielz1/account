package ru.example.account.web.model.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank

                           @Size(min = 8, max = 32, message = "Minimum 8 chars and Maximum 32 chars")
                           String username,

                           @NotBlank
                           @Size(min = 8, max = 32, message = "Minimum 8 chars and Maximum 32 chars")
                           String password) {

}
