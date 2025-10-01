package ru.example.cloudfiles.exception.StorageOperationImpl.directory;

import ru.example.cloudfiles.exception.StorageOperationImpl.resource.StorageOperationException;

public class DirectoryNotExistException extends StorageOperationException {
          private static final String MESSAGE = "Directory does not exist: %s";
          public DirectoryNotExistException(String path) { super(MESSAGE.formatted(path)); }
}