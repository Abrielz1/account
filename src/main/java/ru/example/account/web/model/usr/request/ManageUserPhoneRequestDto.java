package ru.example.account.web.model.usr.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ManageUserPhoneRequestDto(@NotBlank
                                        @Pattern(regexp = "^\\+7\\d{10}$", message = "Invalid phone format")
                                        String phone) {
}
