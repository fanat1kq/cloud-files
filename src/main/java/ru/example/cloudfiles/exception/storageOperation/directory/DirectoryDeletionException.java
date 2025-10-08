package ru.example.cloudfiles.exception.storageOperationImpl.directory;

import ru.example.cloudfiles.exception.storageOperationImpl.resource.StorageOperationException;

import static ru.example.cloudfiles.util.Constants.MESSAGE_RESOURCE_TO_DELETE_DIRECTORY;

public class DirectoryDeletionException extends StorageOperationException {

    public DirectoryDeletionException(String path, Throwable cause) {

        super(MESSAGE_RESOURCE_TO_DELETE_DIRECTORY.formatted(path), cause);
    }
}