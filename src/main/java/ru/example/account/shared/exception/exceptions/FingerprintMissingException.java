package ru.example.account.shared.exception.exceptions;

public class FingerprintMissingException extends RuntimeException {

    public FingerprintMissingException(String message) {
        super(message);
    }
}
