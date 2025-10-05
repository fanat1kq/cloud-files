package ru.example.cloudfiles.exception.storageOperationImpl.resource;

import static ru.example.cloudfiles.util.Constants.MESSAGE_FAILED_TO_CHECK_OBJECT_EXISTENCE;

public class ResourceExistenceCheckException extends StorageOperationException {

    public ResourceExistenceCheckException(String path, Throwable cause) {

        super(MESSAGE_FAILED_TO_CHECK_OBJECT_EXISTENCE.formatted(path), cause);
    }
}
