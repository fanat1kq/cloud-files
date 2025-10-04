package ru.example.cloudfiles.exception.StorageOperationImpl.resource;

public class ResourceRetrievalException extends StorageOperationException {
    public ResourceRetrievalException(String path, Throwable cause) {
        super("Failed to get resource: " + path, cause);
    }
}
