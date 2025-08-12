package ru.example.account.security.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.service.impl.AppUserDetails;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest);

    void logout(AppUserDetails userDetails);

    void logoutAll(AppUserDetails userDetails);

    void trustDevice(Long userId, String fingerprint, HttpServletRequest request);

    String generateAndCacheVerificationCode(Long userId, String fingerprint);

    boolean verifyCode(Long userId, String fingerprint, String code);
}
