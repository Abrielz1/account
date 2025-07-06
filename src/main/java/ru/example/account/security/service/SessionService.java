package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RefreshToken;
import ru.example.account.security.entity.RevocationReason;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис, инкапсулирующий всю низкоуровневую логику
 * по управлению жизненным циклом сессий.
 */
public interface SessionService {

    /**
     * Создает новую сессию и соответствующий ей активный refresh-токен.
     * @param userId ID пользователя.
     * @param fingerprintHash Хэш фингерпринта.
     * @param ipAddress IP адрес.
     * @param userAgent User-Agent.
     * @return Созданный активный RefreshToken, который хранится в Redis.
     */
    RefreshToken createSessionAndToken(Long userId, String fingerprintHash, String ipAddress, String userAgent);

    /**
     * Находит активный refresh-токен в Redis по его значению.
     */
    Optional<RefreshToken> findActiveRefreshToken(String token);

    /**
     * Находит сессию в Postgres по ее ID.
     */
    Optional<AuthSession> findSessionById(UUID sessionId);

    /**
     * Архивирует сессию и связанный с ней refresh-токен.
     * Переносит данные из Redis в архив Postgres и удаляет из Redis.
     * @param token Активный RefreshToken из Redis.
     * @param reason Причина отзыва.
     */
    void archiveAndRevoke(RefreshToken token, RevocationReason reason);

    /**
     * Принудительно отзывает все активные сессии для указанного пользователя.
     */
    void revokeAllForUser(Long userId, RevocationReason reason);
}