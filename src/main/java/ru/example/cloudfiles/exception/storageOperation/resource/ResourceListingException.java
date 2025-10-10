package ru.example.cloudfiles.exception.storageOperation.resource;

import static ru.example.cloudfiles.util.Constants.MESSAGE_FAILED_TO_LIST_OBJECTS_WITH_PREFIX;

public class ResourceListingException extends StorageOperationException {

    public ResourceListingException(String prefix, Throwable cause) {

        super(MESSAGE_FAILED_TO_LIST_OBJECTS_WITH_PREFIX.formatted(prefix), cause);
    }
}
