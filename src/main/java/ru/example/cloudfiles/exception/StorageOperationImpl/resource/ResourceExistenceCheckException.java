package ru.example.cloudfiles.exception.StorageOperationImpl.resource;

public class ResourceExistenceCheckException extends StorageOperationException {
    public ResourceExistenceCheckException(String path, Throwable cause) {
        super("Failed to check object existence: " + path, cause);
    }
}
