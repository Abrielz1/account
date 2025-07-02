package ru.example.account.security.listener;

import com.fasterxml.jackson.databind.ObjectMapper; // Нужен для десериализации
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener; // Используем интерфейс для надежности
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;
import ru.example.account.security.entity.RefreshToken; // Путь к твоей Redis-сущности
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisKeyExpirationListener implements ApplicationListener<RedisKeyExpiredEvent<?>> {

    private final ObjectMapper objectMapper; // Инжектируем для десериализации JSON

    @Override
    public void onApplicationEvent(RedisKeyExpiredEvent<?> event) {

        // 1. Получаем ключ в виде строки. event.getSource() - это ключ.
        String expiredKey = new String(event.getSource());

        // 2. Анализируем ключ по префиксу.
        // `active_refresh_tokens` - это keyspace, который мы задаем в @RedisHash
        if (expiredKey.startsWith("active_refresh_tokens:")) {
            log.info("A refresh token has expired. Key: {}", expiredKey);

            // 3. ПОПЫТАЕМСЯ получить тело, если оно нам нужно для логирования
            Object value = event.getValue();

            // Проверяем, что значение - это массив байт
            if (value instanceof byte[] body) {
                try {
                    // Теперь мы можем попытаться десериализовать его
                    // ВАЖНО: класс должен быть тем, что мы ожидаем
                    RefreshToken expiredToken = objectMapper.readValue(body, RefreshToken.class);
                    log.info("Expired Refresh Token details: userId={}, sessionId={}",
                            expiredToken.getUserId(), expiredToken.getSessionId());

                    // Здесь можно опубликовать свое, уже типизированное, внутреннее событие.
                    // publisher.publishEvent(new RefreshTokenHasExpired(expiredToken.getUserId(), ...));

                } catch (IOException e) {
                    log.error("Could not deserialize expired Redis value for key: {}. Error: {}", expiredKey, e.getMessage());
                }
            } else {
                log.warn("Expired event for key '{}' received, but the value is not a byte array. Value type: {}",
                        expiredKey, (value != null ? value.getClass().getName() : "null"));
            }
        }
        // Здесь можно добавить другие `else if (expiredKey.startsWith(...))` для других типов объектов
    }
}