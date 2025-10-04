package ru.example.cloudfiles.exception.StorageOperationImpl.bucket;

import ru.example.cloudfiles.exception.StorageOperationImpl.resource.StorageOperationException;

public class BucketCreationException extends StorageOperationException {
    private static final String MESSAGE = "Failed to create bucket: %s";

    public BucketCreationException(String bucketName, Throwable cause) {
        super(MESSAGE.formatted(bucketName), cause);
    }

    // Дополнительно можно добавить конструктор без cause
    public BucketCreationException(String bucketName) {
        super(MESSAGE.formatted(bucketName));
    }
}