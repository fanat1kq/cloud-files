package ru.example.cloudfiles.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class FileUploadValidator {

          private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
          private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
                    "image/*", "text/*", "application/pdf", "application/zip"
          );

          public void validateFiles(MultipartFile[] files) {
                    if (files == null || files.length == 0) {
                              throw new IllegalArgumentException("No files provided for upload");
                    }

                    for (MultipartFile file : files) {
                              validateFile(file);
                    }
          }

          private void validateFile(MultipartFile file) {
                    if (file.isEmpty()) {
                              throw new IllegalArgumentException("One or more files are empty");
                    }

                    if (file.getOriginalFilename() == null ||
                              file.getOriginalFilename().isBlank()) {
                              throw new IllegalArgumentException("File name cannot be empty");
                    }

                    if (file.getSize() > MAX_FILE_SIZE) {
                              throw new IllegalArgumentException(
                                        String.format(
                                                  "File size exceeds maximum allowed size of %dMB",
                                                  MAX_FILE_SIZE / (1024 * 1024))
                              );
                    }

                    validateContentType(file.getContentType());
          }

          private void validateContentType(String contentType) {
                    if (contentType == null) {
                              return; // Разрешаем файлы без content-type
                    }

                    boolean allowed = ALLOWED_CONTENT_TYPES.stream()
                              .anyMatch(allowedType -> {
                                        if (allowedType.endsWith("/*")) {
                                                  String baseType = allowedType.substring(0,
                                                            allowedType.length() - 2);
                                                  return contentType.startsWith(baseType);
                                        }
                                        return allowedType.equals(contentType);
                              });

                    if (!allowed) {
                              throw new IllegalArgumentException(
                                        "File type not allowed: " + contentType);
                    }
          }
}