package ru.example.account.security.service.worker;

/**
 * QUERY-ВОРКЕР. "Пограничник".
 * Умеет ТОЛЬКО, сука, отвечать на вопрос: "Этот, девайс - НАШ или ЧУЖОЙ?"
 */
public interface WhitelistDeviceQueryWorker {
    /**
     * Главный, метод проверки "свой-чужой".
     * Реализует эшелонированную, проверку (Redis -> Postgres).
     *
     * @param userId      ID пользователя, который стучится.
     * @param accessToken Access-токен, который он предъявил (для Token Binding в Redis).
     * @param fingerprint "Паспорт" устройства (его "отпечаток").
     * @return true, если устройству можно, доверять.
     */
    boolean isDeviceTrusted(Long userId, String accessToken, String fingerprint);
}
