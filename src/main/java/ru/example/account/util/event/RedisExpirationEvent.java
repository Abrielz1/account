package ru.example.account.util.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;
import ru.example.account.app.entity.RefreshToken;
import ru.example.account.util.exception.exceptions.RefreshTokenException;

@Slf4j
@Component
public class RedisExpirationEvent {

    @EventListener
    public void handelRedisKeyExpiredEvent(RedisKeyExpiredEvent<RefreshToken> event) {

        RefreshToken expiredRefreshToken = (RefreshToken) event.getValue();

        if (expiredRefreshToken == null) {
            throw new RefreshTokenException("Refresh token is null in handleRedisKeyExpiredEvent function");
        }

        log.info(("Refresh token" +
                " has expired and Refresh Token is %s").formatted(expiredRefreshToken.getTokenRefresh()
                ));
    }
}
