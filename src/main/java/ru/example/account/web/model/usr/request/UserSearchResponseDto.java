package ru.example.account.web.model.usr.request;

import java.time.LocalDate;
import java.util.Set;

public record UserSearchResponseDto(String username,
                                    LocalDate dateOfBirth,
                                    Set<String> phones,
                                    Set<String> emails
) {
}
