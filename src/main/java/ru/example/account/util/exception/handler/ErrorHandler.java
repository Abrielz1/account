package ru.example.account.util.exception.handler;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Database constraint violation: {}", ex.getMessage());

        return new ErrorResponse("Data integrity violation", ex.getRootCause().getMessage());
    }

    @ExceptionHandler(PessimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handlePessimisticLockException(PessimisticLockException ex) {
        log.error("Database lock conflict: {}", ex.getMessage());
        return new ErrorResponse("Operation conflicted with another transaction. Try again later.", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handlePessimisticLockException(IllegalStateException ex) {
        log.error("Database lock conflict: {}", ex.getMessage());
        return new ErrorResponse("Users id didnt match!", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return new ErrorResponse(ex.getMessage(), ex.getLocalizedMessage());
    }

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
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyExistsException(AlreadyExistsException ex) {
        log.warn("409 {}", ex.getMessage(), ex);
        return new ErrorResponse("Resource already exists", ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOptimisticLockException(OptimisticLockException ex) {
        log.error("Concurrent modification: {}", ex.getMessage());
        return new ErrorResponse("Data was updated by another user. Please retry.", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return new ErrorResponse("Internal server error", ex.getMessage());
    }
}

