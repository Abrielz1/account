package ru.example.account.shared.exception.exceptions;

public class SerializationException extends RuntimeException {

    public SerializationException(String message) {
        super(message);
    }

    // ВАЖНЕЙШИЙ КОНСТРУКТОР!
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
