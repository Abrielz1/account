package ru.example.account.shared.exception.exceptions;

public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String message) {
        super(message);
    }
}
