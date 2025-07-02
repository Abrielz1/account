package ru.example.account.user.model.request;

import java.time.LocalDate;
import java.util.Set;

public record UserSearchResponseDto(String username,
                                    LocalDate dateOfBirth,
                                    Set<String> phones,
                                    Set<String> emails
) {
}
