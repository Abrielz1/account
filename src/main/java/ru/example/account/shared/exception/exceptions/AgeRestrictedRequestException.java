package ru.example.account.shared.exception.exceptions;

public class AgeRestrictedRequestException extends RuntimeException {

    public AgeRestrictedRequestException(String message) {
        super(message);
    }
}
