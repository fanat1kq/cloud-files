package ru.example.cloudfiles.exception.user;

import static ru.example.cloudfiles.util.Constants.MESSAGE_USER_NOT_FOUND;

public class UserNotFoundException extends RuntimeException {


    public UserNotFoundException(String username) {
        super(MESSAGE_USER_NOT_FOUND.formatted(username));
    }

    public UserNotFoundException(String username, Throwable cause) {
        super(MESSAGE_USER_NOT_FOUND.formatted(username), cause);
    }

    public UserNotFoundException(Long userId) {
        super(MESSAGE_USER_NOT_FOUND.formatted(userId));
    }
}