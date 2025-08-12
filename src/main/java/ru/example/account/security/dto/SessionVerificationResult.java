package ru.example.account.security.dto;

import ru.example.account.security.entity.ActiveSessionCache;
import java.util.Optional;

/**
 * РАПОРТ, "Отчет о проверке сессии".
 * Он, содержит и саму сессию (если нашлась), и ВЕРДИКТ.
 */
public record SessionVerificationResult(

                                        VerificationStatus status,     // <<<--- ВЕРДИКТ!

                                        Optional<ActiveSessionCache> sessionCache // <<<--- САМА СЕССИЯ (может быть пустой)

) {
    /**
     * Статический, "фабричный метод" для УДОБСТВА.
     * Чтобы не писать "new SessionVerificationResult(VerificationStatus.VALID, Optional.of(session))"
     */
    public static SessionVerificationResult valid(ActiveSessionCache session) {
        return new SessionVerificationResult(VerificationStatus.VALID, Optional.of(session));
    }

    public static SessionVerificationResult invalid(VerificationStatus status) {
        // Для всех, провальных случаев, сессия будет пустой.
        return new SessionVerificationResult(status, Optional.empty());
    }
}
