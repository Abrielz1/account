package ru.example.account.web.model.auth.request;

import ru.example.account.app.entity.RoleType;
import java.time.LocalDate;
import java.util.List;

public record UserCredentialsRegisterRequestDto(String email,
                                                String password,
                                                String username,
                                                String phoneNumber,
                                                LocalDate birthDate,
                                                List<RoleType> roles) {
}
