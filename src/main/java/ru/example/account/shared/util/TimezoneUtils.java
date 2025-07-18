package ru.example.account.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.util.TimeZone;

@Slf4j
@Component
public class TimezoneUtils {

    /**
     * Пытается определить таймзону клиента.
     * В идеале, фронтенд должен присылать ее в заголовке X-Time-Zone.
     * Если заголовка нет - фоллбэк на дефолтную таймзону сервера.
     */
    public ZoneId getZoneIdFromRequest(HttpServletRequest request) {
        String timeZoneHeader = request.getHeader("X-Time-Zone");
        try {
            if (timeZoneHeader != null) {
                return ZoneId.of(timeZoneHeader);
            }
        } catch (Exception e) {
            // Клиент прислал некорректную строку
            // Логируем и используем дефолт
        }
        // Фоллбэк, если заголовка нет. Не идеально, но лучше чем ничего.
        return TimeZone.getDefault().toZoneId();
    }
}
