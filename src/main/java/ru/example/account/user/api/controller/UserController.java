package ru.example.account.user.api.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.account.security.service.impl.AppUserDetails;
import ru.example.account.user.service.UserService;
import ru.example.account.security.model.request.ManageUserEmailRequestDto;
import ru.example.account.security.model.request.ManageUserPhoneRequestDto;
import ru.example.account.security.model.request.UserSearchResponseDto;
import ru.example.account.security.model.response.CreateUserAccountDetailResponseDto;
import ru.example.account.security.model.response.UserEmailResponseDto;
import ru.example.account.security.model.response.UserPhoneResponseDto;
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

    @PostMapping("/email")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserAccountDetailResponseDto createUserEmailData(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Validated @RequestBody ManageUserEmailRequestDto updateUser) {
        return userService.createUserEmailData(currentUser, updateUser);
    }

    @PostMapping("/phone")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserAccountDetailResponseDto createUserPhoneData(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Validated @RequestBody ManageUserPhoneRequestDto updateUser) {
        return userService.createUserPhoneData(currentUser, updateUser);
    }

    @PutMapping("/edit/email")
    @ResponseStatus(HttpStatus.OK)
    public UserEmailResponseDto editUserEmailData(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Validated @RequestBody ManageUserEmailRequestDto updateUser) {
        return userService.editUserEmailData(currentUser, updateUser);
    }

    @PutMapping("/edit/phone")
    @ResponseStatus(HttpStatus.OK)
    public UserPhoneResponseDto editUserPhoneData(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Validated @RequestBody ManageUserPhoneRequestDto updateUser) {
        return userService.editUserPhoneData(currentUser, updateUser);
    }
}
