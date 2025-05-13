package ru.example.account.util.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.example.account.util.exception.exceptions.AccessDeniedException;
import ru.example.account.util.exception.exceptions.AlreadyExistsException;
import ru.example.account.util.exception.exceptions.BadRequestException;
import ru.example.account.util.exception.exceptions.UserNotFoundException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlerNotFoundException(final UserNotFoundException e) {

        log.warn("404 {}", e.getMessage(), e);
        return new ErrorResponse("User was not found 404", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerBadRequest(final BadRequestException e) {

        log.warn("400 {}", e.getMessage(), e);
        return new ErrorResponse("Bad request was committed 400 ", e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("403 {}", ex.getMessage(), ex);
        return new ErrorResponse("Access denied", ex.getMessage());
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409 больше подходит для конфликтов
    public ErrorResponse handleAlreadyExistsException(AlreadyExistsException ex) {
        log.warn("409 {}", ex.getMessage(), ex);
        return new ErrorResponse("Resource already exists", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return new ErrorResponse("Internal server error", ex.getMessage());
    }
}

