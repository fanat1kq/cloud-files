package ru.example.cloudfiles.exception.StorageOperationImpl.resource;

public class ResourceDeletionException extends StorageOperationException {
    public ResourceDeletionException(String path, Throwable cause) {
        super("Failed to delete resource: " + path, cause);
    }
}