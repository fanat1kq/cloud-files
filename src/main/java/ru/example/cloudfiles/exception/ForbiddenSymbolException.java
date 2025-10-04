package ru.example.cloudfiles.exception;

import java.util.List;

public class ForbiddenSymbolException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Path contains forbidden characters";
    private static final String MESSAGE_WITH_PATH = "Path '%s' contains forbidden characters: %s";

    public ForbiddenSymbolException() {
        super(DEFAULT_MESSAGE);
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