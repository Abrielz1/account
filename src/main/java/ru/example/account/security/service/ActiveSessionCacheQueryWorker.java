package ru.example.account.security.service;

import org.springframework.security.core.GrantedAuthority;
import ru.example.account.security.dto.SessionVerificationResult;
import java.util.Collection;
import java.util.UUID;

/**
 * QUERY-ВОРКЕР.
 * Умеет ТОЛЬКО, ИСКАТЬ и ПРОВЕРЯТЬ сессии.
 * Реализует, сложную логику "Cache-Aside" с "самоизлечением".
 */
public interface ActiveSessionCacheQueryWorker {
    /**
     * ГЛАВНЫЙ МЕТОД ПОИСКА. "ЭТА СЕССИЯ - ЖИВАЯ И НАША?".
     * Сначала ищет в Redis. Если нет - ищет в Postgres.
     * Если находит в Postgres - "прогревает", кеш.
     * Если данные в Redis и Postgres, бл***, не совпадают - поднимает тревогу.
     *
     * @param sessionId              ID сессии, которую ищем.
     * @param currentUserId          ID юзера из JWT (для сверки "свой-чужой").
     * @param currentUserAuthorities Роли из JWT (для сверки).
     * @return OSessionVerificationResult — "умный" объект, который содержит и ВЕРДИКТ, и саму сессию.
     * Если что-то пошло не так, только пустую сессию.
     */
    SessionVerificationResult findAndVerifySession(
            UUID sessionId,
            Long currentUserId,
            Collection<? extends GrantedAuthority> currentUserAuthorities
    );
}
