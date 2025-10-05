package ru.example.cloudfiles.util;

public final class Constants {

    private Constants() {}

    public static final String MESSAGE_FORBIDDEN_CHARACTERS = "Path contains forbidden characters";
    public static final String MESSAGE_USER_EXIST = "User already exists: %s";
    public static final String MESSAGE_USER_NOT_FOUND = "User not found: %s";
    public static final String MESSAGE_FAILED_TO_CREATE_BUCKET = "Failed to create bucket: %s";
    public static final String MESSAGE_FAILED_TO_DELETE_BUCKET = "Failed to delete bucket: %s";
    public static final String MESSAGE_FAILED_TO_CREATE_DIRECTORY = "Failed to create directory: %s";
    public static final String MESSAGE_DIRECTORY_DOES_NOT_EXIST = "Directory does not exist: %s";
    public static final String MESSAGE_RESOURCE_TO_DELETE_DIRECTORY = "Failed to delete directory: %s";
    public static final String MESSAGE_RESOURCE_ALREADY_EXISTS = "Resource: \"%s\" already exists";
    public static final String MESSAGE_RESOURCE_NOT_FOUND = "Resource: \"%s\" not found";
    public static final String MESSAGE_FAILED_TO_SAVE_RESOURCE = "Failed to save resource: %s";
    public static final String MESSAGE_FAILED_TO_UPLOAD_RESOURCE = "Failed to upload resource: %s";
    public static final String MESSAGE_NOT_A_DIRECTORY = "\"%s\" not a directory";
    public static final String MESSAGE_RESOURCE_DOES_NOT_EXIST = "Resource does not exist: %s";
    public static final String MESSAGE_RESOURCE_TO_DELETE_BUCKET = "Failed to delete resource: %s";
    public static final String MESSAGE_FAILED_TO_CHECK_OBJECT_EXISTENCE = "Failed to check object existence: %s";
    public static final String MESSAGE_FAILED_TO_LIST_OBJECTS_WITH_PREFIX = "Failed to list objects with prefix: ";
    public static final String MESSAGE_FAILED_TO_ADD_FILE_TO_ZIP = "Failed to add file to zip: %s";
    public static final String MESSAGE_EMPTY_PATH = "Path is empty";
}