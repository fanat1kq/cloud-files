package ru.example.cloudfiles.exception.storageOperation.directory;

import static ru.example.cloudfiles.util.Constants.MESSAGE_FAILED_TO_ADD_FILE_TO_ZIP;

public class ZipCreationException extends RuntimeException {

    public ZipCreationException(String name, Throwable cause) {

        super(MESSAGE_FAILED_TO_ADD_FILE_TO_ZIP.formatted(name), cause);
    }
}
