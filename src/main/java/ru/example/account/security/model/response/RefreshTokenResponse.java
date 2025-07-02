package ru.example.account.security.model.response;

public record RefreshTokenResponse(String accessToken,

                                   String tokenRefresh) {
}
