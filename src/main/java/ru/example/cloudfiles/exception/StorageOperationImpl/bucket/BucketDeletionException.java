package ru.example.cloudfiles.exception.StorageOperationImpl.bucket;

import ru.example.cloudfiles.exception.StorageOperationImpl.resource.StorageOperationException;

public class BucketDeletionException extends StorageOperationException {
    private static final String MESSAGE = "Failed to delete bucket: %s";

    public BucketDeletionException(String bucketName, Throwable cause) {
        super(MESSAGE.formatted(bucketName), cause);
    }

    public BucketDeletionException(String bucketName) {
        super(MESSAGE.formatted(bucketName));
    }
}