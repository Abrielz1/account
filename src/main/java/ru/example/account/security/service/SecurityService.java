package ru.example.account.security.service;

import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.request.UserCredentialsRegisterRequestDto;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.model.response.RefreshTokenResponse;
import ru.example.account.security.model.response.UserCredentialsResponseDto;

public interface SecurityService {

    UserCredentialsResponseDto register(UserCredentialsRegisterRequestDto requestDto);

    AuthResponse authenticationUser(LoginRequest loginRequest);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void logout();
}
