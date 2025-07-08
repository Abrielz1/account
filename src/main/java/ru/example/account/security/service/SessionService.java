package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RefreshToken;
import ru.example.account.security.entity.RevocationReason;

import java.util.UUID;

/**
 * Сервис, инкапсулирующий всю низкоуровневую логику
 * по управлению жизненным циклом сессий и их хранилищами (Postgres, Redis).
 * Он скрывает детали взаимодействия с базами данных от высокоуровневых сервисов.
 */
public interface SessionService {

    /**
     * Создает новую сессию в PostgreSQL (AuthSession)
     * и соответствующий ей активный refresh-токен в Redis.
     * Оба объекта связываются по sessionId.
     *
     * @param userId          ID пользователя, владельца сессии.
     * @param fingerprintHash Хэш цифрового отпечатка устройства.
     * @param ipAddress       IP-адрес, с которого сделан запрос.
     * @param userAgent       User-Agent клиента.
     * @return Созданный активный RefreshToken, который уже сохранен в Redis.
     */
    RefreshToken createSessionAndToken(Long userId, String fingerprintHash, String ipAddress, String userAgent);

    /**
     * Находит активный refresh-токен в Redis по его значению.
     *
     * @param token Строка refresh-токена.
     * @return Активный RefreshToken, если найден. В случае отсутствия,
     *          будет выброшено TokenNotFoundException (детали в реализации).
     */
    RefreshToken findActiveRefreshToken(String token);

    /**
     * Находит сессию (AuthSession) в PostgreSQL по ее ID.
     * Используется для получения подробной информации о сессии.
     *
     * @param sessionId ID сессии (UUID).
     * @return AuthSession, если найдена. В случае отсутствия,
     *          будет выброшено SessionNotFoundException (детали в реализации).
     */
    AuthSession findSessionById(UUID sessionId);

    /**
     * Выполняет полный процесс отзыва и архивации сессии и refresh-токена:
     * 1. Архивирует данные токена в PostgreSQL.
     * 2. Обновляет статус сессии (AuthSession) в PostgreSQL.
     * 3. Удаляет активный refresh-токен из Redis.
     * 4. Добавляет access-токен во временный blacklist в Redis.
     *
     * @param session         Объект AuthSession (активный, полученный из findSessionById).
     * @param token           Активный RefreshToken (полученный из findActiveRefreshToken).
     * @param reason          Причина отзыва (из Enum RevocationReason).
     */
    void archiveAndRevoke(AuthSession session, RefreshToken token, RevocationReason reason);

    /**
     * Принудительно отзывает все активные сессии для указанного пользователя.
     * Обычно вызывается администратором или при смене пароля.
     *
     * @param userId ID пользователя, чьи сессии отзываются.
     * @param reason Причина отзыва (обычно REVOKED_BY_SYSTEM или PASSWORD_CHANGE).
     */
    void revokeAllForUser(Long userId, RevocationReason reason);
}