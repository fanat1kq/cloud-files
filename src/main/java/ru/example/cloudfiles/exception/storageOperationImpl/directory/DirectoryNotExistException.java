package ru.example.cloudfiles.exception.storageOperationImpl.directory;

import ru.example.cloudfiles.exception.storageOperationImpl.resource.StorageOperationException;

import static ru.example.cloudfiles.util.Constants.MESSAGE_DIRECTORY_DOES_NOT_EXIST;

public class DirectoryNotExistException extends StorageOperationException {

    public DirectoryNotExistException(String path) {

        super(MESSAGE_DIRECTORY_DOES_NOT_EXIST.formatted(path));
    }
}