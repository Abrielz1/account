package ru.example.account.security.service;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import java.util.UUID;

/**
 * ФАСАД. ЕДИНАЯ, ДИСПЕТЧЕРСКАЯ.
 */
public interface SecurityReactionFacade {

    /**
     * Запускает ВЕСЬ, ПРОТОКОЛ.
     * Принимает ВСЕ возможные улики, которые, МОГУТ БЫТЬ.
     *
     * @param userId             ID, юзера (из токена). МОЖЕТ БЫТЬ NULL.
     * @param sessionId          ID, сессии (если она есть). МОЖЕТ БЫТЬ NULL.
     * @param request            Сырой, HTTP-запрос со всеми "потрохами". ОБЯЗАТЕЛЕН.
     * @param statusToSet        НОВЫЙ, СТАТУС, который мы хотим присвоить (для сессии).
     * @param reason             КОНКРЕТНАЯ, ПРИЧИНА отзыва.
     * @return boolean - true, если протокол отработал без критических сбоев.
     */
    boolean initiateRedAlertProtocol(
            @Nullable Long userId,             // <<<--- нужен для "stateless" атак.
            @Nullable UUID sessionId,          // <<<--- нужен для атак "изнутри".
            HttpServletRequest request,
            SessionStatus statusToSet,
            RevocationReason reason
    );
}
