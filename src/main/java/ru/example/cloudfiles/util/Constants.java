package ru.example.cloudfiles.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public final String MESSAGE_FORBIDDEN_CHARACTERS = "Path contains forbidden characters";
    public final String MESSAGE_WITH_PATH = "Path '%s' contains forbidden characters: %s";
    public final String MESSAGE_USER_EXIST = "User already exists: %s";
    public final String MESSAGE_USER_NOT_FOUND = "User not found: %s";
    public final String MESSAGE_FAILED_TO_CREATE_BUCKET = "Failed to create bucket: %s";
    public final String MESSAGE_FAILED_TO_DELETE_BUCKET = "Failed to delete bucket: %s";
    public final String MESSAGE_FAILED_TO_CREATE_DIRECTORY = "Failed to create directory: %s";
    public final String MESSAGE_DIRECTORY_DOES_NOT_EXIST = "Directory does not exist: %s";
    public final String MESSAGE_RESOURCE_TO_DELETE_DIRECTORY = "Failed to delete directory: %s";
    public final String MESSAGE_RESOURCE_ALREADY_EXISTS = "Resource: \"%s\" already exists";
    public final String MESSAGE_RESOURCE_NOT_FOUND = "Resource: \"%s\" not found";
    public final String MESSAGE_FAILED_TO_SAVE_RESOURCE = "Failed to save resource: %s";
    public final String MESSAGE_FAILED_TO_UPLOAD_RESOURCE = "Failed to upload resource: %s";
    public final String MESSAGE_NOT_A_DIRECTORY = "\"%s\" not a directory";
    public final String MESSAGE_RESOURCE_DOES_NOT_EXIST = "Resource does not exist: %s";
    public final String MESSAGE_RESOURCE_TO_DELETE_BUCKET = "Failed to delete resource: %s";
    public final String MESSAGE_FAILED_TO_CHECK_OBJECT_EXISTENCE = "Failed to check object existence: %s";
    public final String MESSAGE_FAILED_TO_LIST_OBJECTS_WITH_PREFIX = "Failed to list objects with prefix: ";
    public final String MESSAGE_FAILED_TO_ADD_FILE_TO_ZIP = "Failed to add file to zip: %s";
}