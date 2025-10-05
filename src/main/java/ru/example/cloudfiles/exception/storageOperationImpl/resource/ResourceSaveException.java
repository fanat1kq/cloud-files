package ru.example.cloudfiles.exception.storageOperationImpl.resource;

import static ru.example.cloudfiles.util.Constants.MESSAGE_FAILED_TO_SAVE_RESOURCE;

public class ResourceSaveException extends StorageOperationException {


    public ResourceSaveException(String path, Throwable cause) {
        super(MESSAGE_FAILED_TO_SAVE_RESOURCE.formatted(path), cause);
    }
}