package ru.example.cloudfiles.mapper.model;

public record ResourceMappingData(
          String objectName,
          String relativePath,
          long size,
          boolean isDirectory,
          long userId
) {
}