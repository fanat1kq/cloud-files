package ru.example.cloudfiles.exception.storageOperation.resource;

import static ru.example.cloudfiles.util.Constants.MESSAGE_RESOURCE_DOES_NOT_EXIST;

public class ResourceRetrievalException extends StorageOperationException {

    public ResourceRetrievalException(String path, Throwable cause) {

        super(MESSAGE_RESOURCE_DOES_NOT_EXIST.formatted(path), cause);
    }
}
