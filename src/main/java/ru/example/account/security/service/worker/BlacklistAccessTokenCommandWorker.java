package ru.example.account.security.service.worker;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;

/**
 * COMMAND-ВОРКЕР.
 * Умеет ТОЛЬКО, добавлять токены в "горячий" черный список Redis.
 * Он, НИЧЕГО не знает о том, как их "проверять".
 */
public interface BlacklistAccessTokenCommandWorker {

    /**
     * ДОБАВЛЯЕТ access-токен в "горячий" черный список Redis.
     * Не возвращает ничего (void), потому что это "fire-and-forget" команда.
     * Если не получится - он, залогирует, но не уронит весь процесс.
     *
     * @param session вся сессия целиком.
     */
    void blacklistAccessToken(AuthSession session, RevocationReason reason);

    void blacklistAccessToken(String accessToken);
}
