package ru.example.cloudfiles.controller.advice;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.example.cloudfiles.dto.ErrorResponse;
import ru.example.cloudfiles.exception.EmptyPathException;
import ru.example.cloudfiles.exception.ForbiddenSymbolException;
import ru.example.cloudfiles.exception.StorageOperationImpl.bucket.BucketCreationException;
import ru.example.cloudfiles.exception.StorageOperationImpl.bucket.BucketDeletionException;
import ru.example.cloudfiles.exception.StorageOperationImpl.directory.DirectoryCreationException;
import ru.example.cloudfiles.exception.StorageOperationImpl.directory.DirectoryNotExistException;
import ru.example.cloudfiles.exception.StorageOperationImpl.directory.NotDirectoryException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.*;
import ru.example.cloudfiles.exception.UserAlreadyExistsException;
import ru.example.cloudfiles.exception.UserNotFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            EmptyPathException.class,
            ForbiddenSymbolException.class,
            IllegalArgumentException.class,
            NotDirectoryException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Access denied"));
    }

    @ExceptionHandler({
            ResourceNotFoundException.class,
            DirectoryNotExistException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            ResourceAlreadyExistsException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            BadCredentialsException.class,  // ← Добавляем Spring Security исключение
            UserNotFoundException.class     // ← Ваше кастомное
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationErrors(RuntimeException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid credentials"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .toList();

        log.warn("Validation errors: {}", errors);

        String errorMessage = !errors.isEmpty() ? errors.get(0) : "Validation failed";
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler({
            StorageOperationException.class,
            ResourceDeletionException.class,
            ResourceSaveException.class,
            ResourceRetrievalException.class,
            ResourceListingException.class,
            ResourceExistenceCheckException.class,
            BucketCreationException.class,
            BucketDeletionException.class,
            DirectoryCreationException.class
    })
    public ResponseEntity<ErrorResponse> handleStorageErrors(RuntimeException ex) {
        log.error("Storage error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Storage operation failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaught(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error"));
    }
}