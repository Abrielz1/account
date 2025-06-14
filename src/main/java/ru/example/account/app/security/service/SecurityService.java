package ru.example.account.app.security.service;

import ru.example.account.app.entity.User;
import ru.example.account.web.model.auth.request.LoginRequest;
import ru.example.account.web.model.auth.request.RefreshTokenRequest;
import ru.example.account.web.model.auth.request.UserCredentialsRegisterRequestDto;
import ru.example.account.web.model.auth.response.AuthResponse;
import ru.example.account.web.model.auth.response.RefreshTokenResponse;
import ru.example.account.web.model.auth.response.UserCredentialsResponseDto;

public interface SecurityService {

    UserCredentialsResponseDto register(UserCredentialsRegisterRequestDto requestDto);

    AuthResponse authenticationUser(LoginRequest loginRequest);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void logout();

    User getUserByUserId(Long userId);
}
