package ru.example.account.web.model.usr.response;

import java.util.List;

public record UserShortResponseDto(Long id,
                                   String email,
                                   List<String> phone) {
}
