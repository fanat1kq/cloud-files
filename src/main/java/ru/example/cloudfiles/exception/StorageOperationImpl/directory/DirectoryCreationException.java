package ru.example.cloudfiles.exception.StorageOperationImpl.directory;

import ru.example.cloudfiles.exception.StorageOperationImpl.resource.StorageOperationException;

public class DirectoryCreationException extends StorageOperationException {
    private static final String MESSAGE = "Failed to create directory: %s";

    public DirectoryCreationException(String path, Throwable cause) {
        super(MESSAGE.formatted(path), cause);
    }
}