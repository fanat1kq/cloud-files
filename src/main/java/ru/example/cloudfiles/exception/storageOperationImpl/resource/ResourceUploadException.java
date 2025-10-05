package ru.example.cloudfiles.exception.storageOperationImpl.resource;

import static ru.example.cloudfiles.util.Constants.MESSAGE_FAILED_TO_UPLOAD_RESOURCE;

public class ResourceUploadException extends StorageOperationException {


    public ResourceUploadException(String path, Throwable cause) {
        super(MESSAGE_FAILED_TO_UPLOAD_RESOURCE.formatted(path), cause);
    }
}