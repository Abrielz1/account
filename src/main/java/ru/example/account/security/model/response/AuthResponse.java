package ru.example.account.security.model.response;

import java.util.List;

public record AuthResponse(Long id,

                           String token,

                           String tokenRefresh,

                           String username,

                           List<String> roles) {
}
