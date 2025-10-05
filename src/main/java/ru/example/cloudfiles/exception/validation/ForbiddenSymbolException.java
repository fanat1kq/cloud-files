package ru.example.cloudfiles.exception.validation;

import java.util.List;

import static ru.example.cloudfiles.util.Constants.MESSAGE_FORBIDDEN_CHARACTERS;
import static ru.example.cloudfiles.util.Constants.MESSAGE_WITH_PATH;

public class ForbiddenSymbolException extends RuntimeException {

    public ForbiddenSymbolException() {
        super(MESSAGE_FORBIDDEN_CHARACTERS);
    }

    public ForbiddenSymbolException(String message) {
        super(message);
    }

    public ForbiddenSymbolException(String path, String forbiddenSymbols) {
        super(MESSAGE_WITH_PATH.formatted(path, forbiddenSymbols));
    }

    public ForbiddenSymbolException(String path, List<String> forbiddenSymbols) {
        super(MESSAGE_WITH_PATH.formatted(path, String.join(", ", forbiddenSymbols)));
    }
}