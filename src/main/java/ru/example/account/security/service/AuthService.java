package ru.example.account.security.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.service.impl.AppUserDetails;

public interface AuthService {


        /**
         * Аутентифицирует пользователя и создает для него новую сессию и пару токенов.
         * @param request DTO с email и паролем.
         * @param httpRequest Оригинальный запрос для сбора фингерпринта.
         * @return Ответ с access и refresh токенами.
         */
        AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

        /**
         * Обновляет сессию, используя refresh токен.
         * Реализует ротацию токенов и защиту от replay-атак.
         * @param request DTO со старым refresh токеном.
         * @param httpRequest Запрос для проверки фингерпринта.
         * @return Новый AuthResponse с новой парой токенов.
         */
        AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest);

        /**
         * Завершает сессию текущего пользователя.
         * Архивирует refresh-токен и добавляет access-токен в blacklist.
         * @param userDetails Principal текущего пользователя, из которого берется sessionId.
         */
        void logout(AppUserDetails currentUser);
}
