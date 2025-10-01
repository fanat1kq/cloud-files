package ru.example.cloudfiles.exception.StorageOperationImpl.resource;

public class ResourceSaveException extends StorageOperationException {
          private static final String MESSAGE = "Failed to save resource: %s";

          public ResourceSaveException(String path, Throwable cause) {
                    super(MESSAGE.formatted(path), cause);
          }

          public ResourceSaveException(String path) {
                    super(MESSAGE.formatted(path));
          }
}