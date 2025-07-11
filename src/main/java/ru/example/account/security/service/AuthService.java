package ru.example.account.security.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.request.UserRegisterRequestDto;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.service.impl.AppUserDetails;
import ru.example.account.user.model.response.CreateUserAccountDetailResponseDto;

public interface AuthService {

    CreateUserAccountDetailResponseDto registerNewUserAccount(UserRegisterRequestDto request);

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(AppUserDetails userDetails);

    void logoutAll(Long userId);
}
