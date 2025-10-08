package ru.example.cloudfiles.exception.storageOperationImpl.resource;

import static ru.example.cloudfiles.util.Constants.MESSAGE_RESOURCE_ALREADY_EXISTS;

public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String path) {

        super(MESSAGE_RESOURCE_ALREADY_EXISTS.formatted(path));
    }
}
