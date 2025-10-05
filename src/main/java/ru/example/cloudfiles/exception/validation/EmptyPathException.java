package ru.example.cloudfiles.exception.validation;

import static ru.example.cloudfiles.util.Constants.MESSAGE_EMPTY_PATH;

public class EmptyPathException extends RuntimeException {

    public EmptyPathException() {

        super(MESSAGE_EMPTY_PATH);
    }
}