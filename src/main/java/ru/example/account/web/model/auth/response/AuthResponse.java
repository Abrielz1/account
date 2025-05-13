package ru.example.account.web.model.auth.response;

import java.util.List;

public record AuthResponse(Long id,

                           String token,

                           String tokenRefresh,

                           String username,

                           List<String> roles) {
}
