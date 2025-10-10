package ru.example.cloudfiles.exception.storageOperation.directory;

import ru.example.cloudfiles.exception.storageOperation.resource.StorageOperationException;

import static ru.example.cloudfiles.util.Constants.MESSAGE_NOT_A_DIRECTORY;

public class NotDirectoryException extends StorageOperationException {

    public NotDirectoryException(String path) {
        super(MESSAGE_NOT_A_DIRECTORY.formatted(path));
    }
}