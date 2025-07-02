package ru.example.account.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.example.account.security.service.impl.AppUserDetails;
import ru.example.account.security.model.request.ManageUserEmailRequestDto;
import ru.example.account.security.model.request.ManageUserPhoneRequestDto;
import ru.example.account.security.model.request.UserSearchResponseDto;
import ru.example.account.security.model.response.CreateUserAccountDetailResponseDto;
import ru.example.account.security.model.response.UserEmailResponseDto;
import ru.example.account.security.model.response.UserPhoneResponseDto;
import java.time.LocalDate;

public interface UserService {

    Page<UserSearchResponseDto> searchUsers(LocalDate dateOfBirth, String phone, String name, String email, PageRequest page);

    CreateUserAccountDetailResponseDto createUserEmailData(AppUserDetails currentUser, ManageUserEmailRequestDto updateUser);

    CreateUserAccountDetailResponseDto createUserPhoneData(AppUserDetails currentUser, ManageUserPhoneRequestDto updateUser);

    UserEmailResponseDto editUserEmailData(AppUserDetails currentUser, ManageUserEmailRequestDto updateUser);

    UserPhoneResponseDto editUserPhoneData(AppUserDetails currentUser, ManageUserPhoneRequestDto updateUser);
}
