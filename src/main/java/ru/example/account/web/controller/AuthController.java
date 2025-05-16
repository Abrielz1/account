package ru.example.account.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
/**
 * Контроллер аутентификации и управления токенами.
 *
 * @endpoint /api/v1/auth
 */
@Tag(name = "Authentication", description = "User authentication and token management")
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SecurityService securityService;

    /**
     * Аутентификация пользователя.
     *
     * @param loginRequest Логин и пароль
     * @return JWT-токены и данные пользователя
     *
     * @response 200 Успешная аутентификация
     * @response 401 Неверные учетные данные
     */

    @Operation(
            summary = "User login",
            description = "Authenticate user and return JWT tokens",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    @PostMapping("/signing")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {

        log.info("User {} logged in at {}", loginRequest.email(), LocalDateTime.now());
        return securityService.authenticationUser(loginRequest);
    }

    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public RefreshTokenResponse refreshTokenRefresh(@RequestBody RefreshTokenRequest request) {
        log.info("Via AuthController RefreshToken refreshed with token {}", request) ;
        return securityService.refreshToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public String logoutOfCurrentAccount() {
        log.info("Via AuthController User logout from account at time: "
                + LocalDateTime.now());
        securityService.logout();
        return "User logged out!";
    }
}
