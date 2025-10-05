package ru.example.cloudfiles.exception.storageOperationImpl.directory;

import ru.example.cloudfiles.exception.storageOperationImpl.resource.StorageOperationException;

import static ru.example.cloudfiles.util.Constants.MESSAGE_FAILED_TO_CREATE_DIRECTORY;

public class DirectoryCreationException extends StorageOperationException {

    public DirectoryCreationException(String path, Throwable cause) {

        super(MESSAGE_FAILED_TO_CREATE_DIRECTORY.formatted(path), cause);
    }
}