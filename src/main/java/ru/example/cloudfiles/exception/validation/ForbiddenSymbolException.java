package ru.example.cloudfiles.exception.validation;

import static ru.example.cloudfiles.util.Constants.MESSAGE_FORBIDDEN_CHARACTERS;

public class ForbiddenSymbolException extends RuntimeException {

    public ForbiddenSymbolException() {
        super(MESSAGE_FORBIDDEN_CHARACTERS);
    }
}