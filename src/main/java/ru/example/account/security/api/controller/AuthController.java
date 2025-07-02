package ru.example.account.security.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.example.account.security.service.SecurityService;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.request.UserCredentialsRegisterRequestDto;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.model.response.RefreshTokenResponse;
import ru.example.account.security.model.response.UserCredentialsResponseDto;

@Slf4j
@Tag(name = "Authentication", description = "User authentication and token management")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SecurityService securityService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public UserCredentialsResponseDto registerUser(@Valid @RequestBody UserCredentialsRegisterRequestDto request) {
        log.info("User registration attempt: {}", request.email());
        return securityService.register(request);
    }

    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login requested: {}", loginRequest.email());
        return securityService.authenticationUser(loginRequest);
    }

    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public RefreshTokenResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Refresh token used: {}", request);
        return securityService.refreshToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public String logoutOfCurrentAccount() {
        securityService.logout();
        log.info("User logged out");
        return "User logged out!";
    }
}
