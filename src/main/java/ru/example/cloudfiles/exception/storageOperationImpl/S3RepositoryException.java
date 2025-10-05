package ru.example.cloudfiles.exception.storageOperationImpl;

public class S3RepositoryException extends RuntimeException {

    public S3RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}