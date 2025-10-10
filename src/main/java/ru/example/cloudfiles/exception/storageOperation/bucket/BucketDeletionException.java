package ru.example.cloudfiles.exception.storageOperation.bucket;

import ru.example.cloudfiles.exception.storageOperation.resource.StorageOperationException;

import static ru.example.cloudfiles.util.Constants.MESSAGE_FAILED_TO_DELETE_BUCKET;


public class BucketDeletionException extends StorageOperationException {

    public BucketDeletionException(String bucketName, Throwable cause) {

        super(MESSAGE_FAILED_TO_DELETE_BUCKET.formatted(bucketName), cause);
    }
}