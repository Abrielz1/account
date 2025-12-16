package ru.example.account.security.service;

import ru.example.account.security.entity.enums.RevocationReason;
import ru.example.account.security.entity.enums.SessionStatus;
import jakarta.servlet.http.HttpServletRequest;

/**
 * ОРКЕСТРАТОР.
 * Реализует сложный, многошаговый бизнес-процесс реагирования.
 */
interface SecurityIncidentOrchestrator {

    /**
     * ГЛАВНЫЙМЕТОД.
     * Инициирует ЗАЧИСТКУ сессий пользователя, ПРЕДВАРИТЕЛЬНО,
     *  СОБРАВ ВСЕ УЛИКИ, ЗАВЕДЯ "ДЕЛО" и ЗАБАНИВ ВИНОВНЫХ.
     *
     * @param userId             ID, целевого пользователя, которого "чистим". ОБЯЗАТЕЛЕН.
         * @param initialStatus      Статус, который присвоить блокируемым сессиям.
     * @param initialReason      Причина, всей этой "вечеринки".
     * @param triggerRequest     Сырой HTTP-запрос, который спровоцировал реакцию.
     *                           Из него "воркеры" вытащат IP, фингерпринт и т.д.
     * @return boolean           - true, если ВЕСЬ, процесс прошел успешно.
     */
    boolean revokeAllUserSessionsAndPunishCulprits(
            Long userId,
            SessionStatus initialStatus,
            RevocationReason initialReason,
            HttpServletRequest triggerRequest
    );
}
