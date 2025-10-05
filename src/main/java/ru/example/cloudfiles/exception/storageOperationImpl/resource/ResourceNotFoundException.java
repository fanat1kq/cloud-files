package ru.example.cloudfiles.exception.storageOperationImpl.resource;

import static ru.example.cloudfiles.util.Constants.MESSAGE_RESOURCE_NOT_FOUND;

public class ResourceNotFoundException extends RuntimeException {


    public ResourceNotFoundException(String resourcePath) {

        super(MESSAGE_RESOURCE_NOT_FOUND.formatted(resourcePath));
    }
}
