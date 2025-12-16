package ru.example.account.security.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.example.account.security.entity.enums.EventType;
import ru.example.account.security.entity.SecurityEvent;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityEventPublisher {


    private final ApplicationEventPublisher publisher;

    public void publish(EventType type, Long userId, UUID sessionId, HttpServletRequest request) {
        publish(type, userId, sessionId, request, Collections.emptyMap());
    }

    public void publish(EventType type, Long userId, UUID sessionId, HttpServletRequest request, Map<String, ? extends Serializable> details) {
        String ip = (request != null) ? request.getRemoteAddr() : "N/A";
        String ua = (request != null) ? request.getHeader("User-Agent") : "N/A";
        publisher.publishEvent(new SecurityEvent(type, userId, sessionId, ip, ua, details));
    }
}
