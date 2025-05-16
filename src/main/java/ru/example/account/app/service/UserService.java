package ru.example.account.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.web.model.usr.request.ManageUserEmailRequestDto;
import ru.example.account.web.model.usr.request.ManageUserPhoneRequestDto;
import ru.example.account.web.model.usr.request.UserSearchResponseDto;
import ru.example.account.web.model.usr.response.CreateUserAccountDetailResponseDto;
import ru.example.account.web.model.usr.response.UserEmailResponseDto;
import ru.example.account.web.model.usr.response.UserPhoneResponseDto;
import java.time.LocalDate;

public interface UserService {

    Page<UserSearchResponseDto> searchUsers(LocalDate dateOfBirth, String phone, String name, String email, PageRequest page);

    CreateUserAccountDetailResponseDto createUserEmailData(AppUserDetails currentUser, ManageUserEmailRequestDto updateUser);

    CreateUserAccountDetailResponseDto createUserPhoneData(AppUserDetails currentUser, ManageUserPhoneRequestDto updateUser);

    UserEmailResponseDto editUserEmailData(AppUserDetails currentUser, ManageUserEmailRequestDto updateUser);

    UserPhoneResponseDto editUserPhoneData(AppUserDetails currentUser, ManageUserPhoneRequestDto updateUser);
}
