package ru.example.cloudfiles.exception;

public class EmptyPathException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Path cannot be empty or null";

    public EmptyPathException() {
        super(DEFAULT_MESSAGE);
    }

    public EmptyPathException(String message) {
        super(message);
    }
}