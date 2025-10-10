package ru.example.cloudfiles.exception.storageOperation.resource;

import static ru.example.cloudfiles.util.Constants.MESSAGE_RESOURCE_TO_DELETE_BUCKET;

public class ResourceDeletionException extends StorageOperationException {

    public ResourceDeletionException(String path, Throwable cause) {

        super(MESSAGE_RESOURCE_TO_DELETE_BUCKET.formatted(path), cause);
    }
}