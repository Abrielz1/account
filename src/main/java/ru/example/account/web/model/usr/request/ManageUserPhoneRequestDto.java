package ru.example.account.web.model.usr.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ManageUserPhoneRequestDto(
        @Schema(description = "New phone number", example = "+79876543210")
        @NotBlank(message = "Phone number cannot be blank")
        @Pattern(regexp = "^\\+7\\d{10}$", message = "Invalid phone format. Must be +7XXXXXXXXXX")
        String phone
) {
}