package ru.example.account.security.service.worker;

import jakarta.servlet.http.HttpServletRequest;

/**
 * COMMAND-ВОРКЕР. "Сотрудник паспортного стола".
 * Умеет ТОЛЬКО, ВЫДАВАТЬ и АННУЛИРОВАТЬ "пропуска" (доверенные отпечатки).
 */
public interface WhitelistCommandWorker {
    /**
     * Регистрирует новое устройство как "доверенное".
     * Создает или обновляет профиль, сохраняет отпечаток в Postgres и "прогревает" кеш.
     * Возвращает true, если операция прошла успешно.
     */
    boolean trustDevice(Long userId, HttpServletRequest request, String accessToken);

    /**
     * АННУЛИРУЕТ "пропуск" для устройства.
     * Удаляет из Redis и помечает в Postgres как is_trusted = false.
     * Возвращает true, если операция прошла успешно.
     */
    boolean unTrustDevice(String fingerprint);
}
