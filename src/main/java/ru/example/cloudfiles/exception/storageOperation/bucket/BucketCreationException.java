package ru.example.cloudfiles.exception.storageOperation.bucket;

import ru.example.cloudfiles.exception.storageOperation.resource.StorageOperationException;

import static ru.example.cloudfiles.util.Constants.MESSAGE_FAILED_TO_CREATE_BUCKET;


public class BucketCreationException extends StorageOperationException {

    public BucketCreationException(String bucketName, Throwable cause) {

        super(MESSAGE_FAILED_TO_CREATE_BUCKET.formatted(bucketName), cause);
    }
}