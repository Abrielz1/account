package ru.example.account.shared.exception.exceptions;

public class EncryptionDataFailureException extends RuntimeException {

    public EncryptionDataFailureException(String message) {
        super(message);
    }
}
