package ru.example.account.security.dto;

public enum VerificationStatus {
    VALID,                      // Всё, снормально, ситуация штатная.
    SESSION_NOT_FOUND,          // "Штатный отлуп": те такой сессии среди активных нет
    IMPOSTER_DETECTED,          // "КРАСНАЯ ТРЕВОГА!": userId в токенах (или токене) и сессии - РАЗНЫЕ.
    CACHE_DATA_STALE            // "Желтая тревога": userId совпали, но ДРУГИЕ ДАННЫЕ - НЕТ.
}
