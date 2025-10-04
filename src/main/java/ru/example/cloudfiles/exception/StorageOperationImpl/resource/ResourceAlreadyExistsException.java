package ru.example.cloudfiles.exception.StorageOperationImpl.resource;

public class ResourceAlreadyExistsException extends RuntimeException {

    private static final String MESSAGE = "Resource: \"%s\" already exists";

    public ResourceAlreadyExistsException(String path) {

        super(MESSAGE.formatted(path));
    }
}
