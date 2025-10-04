package ru.example.cloudfiles.exception;

public class UserNotFoundException extends RuntimeException {
    private static final String MESSAGE = "User not found: %s";

    public UserNotFoundException(String username) {
        super(MESSAGE.formatted(username));
    }

    public UserNotFoundException(String username, Throwable cause) {
        super(MESSAGE.formatted(username), cause);
    }

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }
}