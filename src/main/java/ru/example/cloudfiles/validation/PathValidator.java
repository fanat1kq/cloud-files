package ru.example.cloudfiles.validation;

import org.springframework.stereotype.Component;

@Component
public class PathValidator {

          public void validatePath(String path) {

                    if (path == null || path.isBlank()) {
                              throw new IllegalArgumentException("Path cannot be null or empty");
                    }
                    if (path.contains("..")) {
                              throw new IllegalArgumentException("Path cannot contain '..'");
                    }
                    if (path.contains("//")) {
                              throw new IllegalArgumentException(
                                        "Path cannot contain consecutive slashes");
                    }
          }

          public void validateFileName(String fileName) {

                    if (fileName == null || fileName.isBlank()) {
                              throw new IllegalArgumentException(
                                        "File name cannot be null or empty");
                    }
                    if (fileName.contains("/") || fileName.contains("\\")) {
                              throw new IllegalArgumentException(
                                        "File name cannot contain path separators");
                    }
                    if (fileName.matches(".*[<>:\"|?*].*")) {
                              throw new IllegalArgumentException(
                                        "File name contains invalid characters");
                    }
          }

          public void validateMovePaths(String fromPath, String toPath) {
                    validatePath(fromPath);
                    validatePath(toPath);

                    if (fromPath.equals(toPath)) {
                              throw new IllegalArgumentException(
                                        "Source and destination paths cannot be the same");
                    }

                    if (fromPath.endsWith("/") && toPath.startsWith(fromPath)) {
                              throw new IllegalArgumentException(
                                        "Cannot move directory into itself");
                    }
          }
}