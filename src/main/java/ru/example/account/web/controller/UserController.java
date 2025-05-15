package ru.example.account.web.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.account.app.service.UserService;
import ru.example.account.web.model.usr.request.UpdateUserAccountDetailRequestDto;
import ru.example.account.web.model.usr.request.UserSearchResponseDto;
import ru.example.account.web.model.usr.response.UserShortResponseDto;
import java.time.LocalDate;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public Page<UserSearchResponseDto> searchUsers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size) {

        return userService.searchUsers(dateOfBirth, phone, name, email, PageRequest.of(page, size));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UserShortResponseDto updateUserData(@Validated @RequestBody UpdateUserAccountDetailRequestDto updateUser) {
        // todo: UPDATE операции для пользователя. Пользователь может менять только собственные данные:
        //       может удалить/сменить/добавить email если он не занят другим пользователям
        //       может удалить/сменить/добавить phone если он не занят другим пользователям
        return null;
    }
}
