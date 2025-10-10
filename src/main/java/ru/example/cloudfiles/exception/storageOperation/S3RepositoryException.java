package ru.example.cloudfiles.exception.storageOperation;

public class S3RepositoryException extends RuntimeException {

    public S3RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}