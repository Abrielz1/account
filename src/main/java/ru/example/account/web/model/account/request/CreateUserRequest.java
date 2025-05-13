package ru.example.account.web.model.account.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.example.account.app.entity.RoleType;
import java.util.Set;


public record CreateUserRequest(@NotBlank
                                @Size(min = 8, max = 32, message = "Username must be 8-32 chars")
                                String username,

                                @NotBlank
                                @Email
                                @Size(min = 12, max = 32, message = "Must be email with 12-32 chars")
                                String email,

                                @NotBlank
                                @Size(min = 8, max = 32, message = "Minimum 8 chars")
                                String password,

                                @NotBlank
                                @Size(min = 1, max = 32, message = "Minimum 1 chars")
                                String firstName,

                                @NotBlank
                                @Size(min = 1, max = 32, message = "Minimum 1 chars")
                                String lastName,

                                @NotNull(message = "Roles are important!")
                                Set<RoleType> roles) {

}
