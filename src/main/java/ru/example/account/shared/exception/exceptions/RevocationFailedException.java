package ru.example.account.shared.exception.exceptions;

public class RevocationFailedException extends RuntimeException {

    public RevocationFailedException(String message) {
        super(message);
    }
}
