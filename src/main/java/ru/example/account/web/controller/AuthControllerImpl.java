package ru.example.account.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.account.app.security.service.SecurityService;
import ru.example.account.web.model.auth.request.LoginRequest;
import ru.example.account.web.model.auth.request.RefreshTokenRequest;
import ru.example.account.web.model.auth.response.AuthResponse;
import ru.example.account.web.model.auth.response.RefreshTokenResponse;
import java.time.LocalDateTime;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllerImpl {

    private final SecurityService securityService;

    @PostMapping("/signing")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {

        log.info("%nVia AuthController User login with account %s at time:"
                .formatted(loginRequest) +  LocalDateTime.now() + "\n");
        return securityService.authenticationUser(loginRequest);
    }

    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public RefreshTokenResponse refreshTokenRefresh(@RequestBody RefreshTokenRequest request) {
        log.info("%nVia AuthController RefreshToken refreshed with token %s at time:"
                .formatted(request) +  LocalDateTime.now() + "\n");
        return securityService.refreshToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public String logoutOfCurrentAccount() {
        log.info("%nVia AuthController User logout from account at time:"
                + LocalDateTime.now() + "\n");
        securityService.logout();
        return "User logged out!";
    }
}
