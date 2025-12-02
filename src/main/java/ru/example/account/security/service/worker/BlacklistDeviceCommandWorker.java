package ru.example.account.security.service.worker;

public interface BlacklistDeviceCommandWorker {

    /**
     * АННУЛИРУЕТ "пропуск" для устройства.
     * Удаляет из Redis и помечает в Postgres как is_trusted = false.
     * Возвращает true, если операция прошла успешно.
     */
    boolean unTrustDevice(String fingerprint);
}
