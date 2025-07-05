package ru.example.account.shared.exception.exceptions;

import java.util.UUID;

public class SecurityBreachAttemptException extends RuntimeException {

    public SecurityBreachAttemptException(String message) {
        super(message);
    }

    public SecurityBreachAttemptException(String message, Long userId, UUID id, String remoteAddr) {

    }
}
