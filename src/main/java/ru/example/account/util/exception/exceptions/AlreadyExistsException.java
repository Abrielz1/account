package ru.example.account.util.exception.exceptions;

public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String message) {
        super(message);
    }
}
