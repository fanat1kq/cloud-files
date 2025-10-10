package ru.example.cloudfiles.exception.storageOperation.resource;

public class StorageOperationException extends RuntimeException {
    public StorageOperationException(String message) {
        super(message);
    }

    public StorageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
