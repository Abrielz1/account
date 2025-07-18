package ru.example.account.user.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.example.account.security.model.request.UserRegisterRequestDto;
import ru.example.account.user.model.response.CreateUserAccountDetailResponseDto;

public interface ClientService {

    CreateUserAccountDetailResponseDto registerNewUser(UserRegisterRequestDto request, HttpServletRequest httpRequest);

//    Page<UserSearchResponseDto> searchUsers(LocalDate dateOfBirth, String phone, String name, String email, PageRequest page);
//
//    CreateUserAccountDetailResponseDto createUserEmailData(AppUserDetails currentUser, ManageUserEmailRequestDto updateUser);
//
//    CreateUserAccountDetailResponseDto createUserPhoneData(AppUserDetails currentUser, ManageUserPhoneRequestDto updateUser);
//
//    UserEmailResponseDto editUserEmailData(AppUserDetails currentUser, ManageUserEmailRequestDto updateUser);
//
//    UserPhoneResponseDto editUserPhoneData(AppUserDetails currentUser, ManageUserPhoneRequestDto updateUser);
}
