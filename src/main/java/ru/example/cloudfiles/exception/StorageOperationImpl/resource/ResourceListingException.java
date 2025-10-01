package ru.example.cloudfiles.exception.StorageOperationImpl.resource;

public class ResourceListingException extends StorageOperationException {
          public ResourceListingException(String prefix, Throwable cause) {
                    super("Failed to list objects with prefix: " + prefix, cause);
          }
}
