package ru.example.account.shared.exception.exceptions;

public class ProcessingInterruptedException extends RuntimeException {

    public ProcessingInterruptedException(String message) {
        super(message);
    }
}
