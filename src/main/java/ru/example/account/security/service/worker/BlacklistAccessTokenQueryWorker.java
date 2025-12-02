package ru.example.account.security.service.worker;

/**
 * QUERY-ВОРКЕР.
 * Умеет ТОЛЬКО отвечать на вопрос: "Этот, бл***, токен - в черном списке?"
 * Он, НИЧЕГО, НЕ МЕНЯЕТ.
 */
public interface BlacklistAccessTokenQueryWorker {
    /**
     * Проверяет "горячий" (Redis) и "холодный" (Postgres) черные списки.
     * @param accessToken "Сырой" access-токен.
     * @return true, если токен отозван.
     */
    boolean isAccessTokenBlacklisted(String accessToken);
}
