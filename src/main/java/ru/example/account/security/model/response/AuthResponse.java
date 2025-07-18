package ru.example.account.security.model.response;

public record AuthResponse(

                           String accessToken,

                           String tokenRefresh) {
}
