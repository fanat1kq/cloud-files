package ru.example.cloudfiles.exception;

public class UserAlreadyExistsException extends RuntimeException {
    private static final String MESSAGE = "User already exists: %s";

    public UserAlreadyExistsException(String username) {
        super(MESSAGE.formatted(username));
    }
}