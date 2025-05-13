package ru.example.account.app.security.service;

import ru.example.account.web.model.auth.request.LoginRequest;
import ru.example.account.web.model.auth.request.RefreshTokenRequest;
import ru.example.account.web.model.auth.response.AuthResponse;
import ru.example.account.web.model.auth.response.RefreshTokenResponse;

public interface SecurityService {
    AuthResponse authenticationUser(LoginRequest loginRequest);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void logout();
}
