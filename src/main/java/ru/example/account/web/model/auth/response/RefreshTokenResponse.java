package ru.example.account.web.model.auth.response;

public record RefreshTokenResponse(String accessToken,

                                   String tokenRefresh) {
}
