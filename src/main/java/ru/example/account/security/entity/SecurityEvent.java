package ru.example.account.security.entity;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Getter
public class SecurityEvent extends ApplicationEvent {

    private final EventType eventType;
    private final Long userId;
    private final UUID sessionId;
    private final String ipAddress;
    private final String userAgent;
    private final Map<String, ? extends Serializable> details;

    public SecurityEvent(EventType type, Long userId, UUID sessionId, String ip, String ua, Map<String, ? extends Serializable> details) {
        super(type); // В качестве source передаем сам тип события
        this.eventType = type;
        this.userId = userId;
        this.sessionId = sessionId;
        this.ipAddress = ip;
        this.userAgent = ua;
        this.details = details;
    }
}
