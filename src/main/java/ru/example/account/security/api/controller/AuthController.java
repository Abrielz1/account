package ru.example.account.security.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.service.AuthService;
import ru.example.account.security.service.impl.AppUserDetails;

@Slf4j
@Tag(name = "Authentication", description = "User authentication and token management")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signing")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        log.info("Login requested: {}", loginRequest.email());
        return authService.login(loginRequest, request);
    }

    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse refreshToken(@RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        log.info("Refresh token used: {}", request);
        return authService.refresh(request, httpRequest);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public String logoutOfCurrentAccount(@AuthenticationPrincipal AppUserDetails currentUser) {
        this.authService.logout(currentUser);
        log.info("User logged out");
        return "User logged out!";
    }
}
