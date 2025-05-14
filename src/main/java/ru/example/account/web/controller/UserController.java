package ru.example.account.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.account.app.service.UserService;
import ru.example.account.web.model.usr.request.UpdateUserAccountDetailRequestDto;
import ru.example.account.web.model.usr.response.UserShortResponseDto;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping // todo criteria api
    @ResponseStatus(HttpStatus.OK)
    public UserShortResponseDto getAllUsersList() {
        // todo: Реализовать READ операцию для пользователей. Сделать «поиск пользователей» (искать может любой любого) с фильтрацией по полям ниже и пагинацией (size, page/offset):
        //       Если передана «dateOfBirth», то фильтр записей, где «date_of_birth» больше чем переданный в запросе.
        //       Если передан «phone», то фильтр по 100% сходству.
        //       Если передан «name», то фильтр по like форматом ‘{text-from-request-param}%’
        //       Если передан «email», то фильтр по 100% сходству.
        return null;
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
