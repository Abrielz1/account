package ru.example.account.security.entity;

public enum BlockReason {
    // Автоматические причины
    BRUTE_FORCE_ATTEMPT,         // Слишком много неудачных попыток входа
    REPLAY_ATTACK_DETECTED,      // Обнаружена атака повтором
    SUSPICIOUS_ACTIVITY,         // Поведенческая аналитика зафиксировала аномалию

    // Ручные причины
    MANUAL_ADMIN_BLOCK,          // Бан по решению администратора
    USER_REQUESTED_BLOCK,        // Блокировка по просьбе самого пользователя
    TERMS_OF_SERVICE_VIOLATION,  // Нарушение правил пользования
    FRAUDULENT_ACTIVITY          // Мошеннические действия
}
