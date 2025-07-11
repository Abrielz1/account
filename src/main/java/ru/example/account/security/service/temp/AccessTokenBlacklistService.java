package ru.example.account.security.service.temp;

import java.time.Duration;
import java.util.UUID;

/**
 * Сервис для управления "горячим" черным списком отозванных access-токенов.
 * Использует Redis для временного хранения.
 */
public interface AccessTokenBlacklistService {

    /**
     * Добавляет ID сессии в черный список на указанное время.
     * @param sessionId ID сессии (из jti claim токена), которую нужно заблокировать.
     * @param duration  Оставшееся время жизни access-токена.
     */
    void addToBlacklist(UUID sessionId, Duration duration);

    /**
     * Проверяет, находится ли ID сессии в черном списке.
     * @param sessionId ID сессии для проверки.
     * @return true, если сессия заблокирована, иначе false.
     */
    boolean isBlacklisted(UUID sessionId);
}
