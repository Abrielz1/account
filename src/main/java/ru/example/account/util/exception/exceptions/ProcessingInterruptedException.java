package ru.example.account.util.exception.exceptions;

public class ProcessingInterruptedException extends RuntimeException {

    public ProcessingInterruptedException(String message) {
        super(message);
    }
}
