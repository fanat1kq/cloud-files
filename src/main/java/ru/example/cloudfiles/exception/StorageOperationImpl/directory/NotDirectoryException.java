package ru.example.cloudfiles.exception.StorageOperationImpl.directory;

import ru.example.cloudfiles.exception.StorageOperationImpl.resource.StorageOperationException;

public class NotDirectoryException extends StorageOperationException {
          private static final String MESSAGE = "\"%s\" not a directory";

          public NotDirectoryException(String path) {
                    super(MESSAGE.formatted(path));
          }

          // Дополнительно можно добавить конструктор с cause
          public NotDirectoryException(String path, Throwable cause) {
                    super(MESSAGE.formatted(path), cause);
          }
}