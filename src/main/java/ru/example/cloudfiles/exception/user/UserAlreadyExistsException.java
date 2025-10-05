package ru.example.cloudfiles.exception.user;

import static ru.example.cloudfiles.util.Constants.MESSAGE_USER_EXIST;

public class UserAlreadyExistsException extends RuntimeException {


    public UserAlreadyExistsException(String username) {
        super(MESSAGE_USER_EXIST.formatted(username));
    }
}