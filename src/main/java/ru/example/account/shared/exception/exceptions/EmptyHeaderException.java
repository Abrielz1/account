package ru.example.account.shared.exception.exceptions;

public class EmptyHeaderException extends  RuntimeException{

    public EmptyHeaderException(String message) {
        super(message);
    }
}
