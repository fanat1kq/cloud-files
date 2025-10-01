package ru.example.cloudfiles.service.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.util.FilePathUtils;


@Service
@RequiredArgsConstructor
public class PathResolver {

          private static final String USER_DIRECTORY = "user-%d-files/";

          private final FilePathUtils filePathUtils;

          private final MinioService minioService;

          public String resolveUserPath(long userId, String path) {

                    if (path == null || path.isBlank() || path.equals("/")) {
                              return USER_DIRECTORY.formatted(userId);
                    }

                    String cleanPath = path.startsWith("/") ? path.substring(1) : path;
                    return USER_DIRECTORY.formatted(userId) + cleanPath;
          }

          public String resolveAndNormalize(long userId, String path) {
                    String normalized = filePathUtils.normalizePath(path);
                    return resolveUserPath(userId, normalized);
          }

          public String resolveAndNormalizeDirectory(long userId, String path) {
                    String normalized = filePathUtils.normalizeDirectoryPath(path);
                    return resolveUserPath(userId, normalized);
          }

          public void ensureUserDirectoryExists(long userId) throws Exception {
                    String userDir = USER_DIRECTORY.formatted(userId);
                    if (!minioService.objectExists(userDir)) {
                              minioService.createDirectory(userDir);
                    }
          }

          public void ensurePathExists(String fullPath, String userPath) throws Exception {
                    if (!minioService.objectExists(fullPath)) {
                              throw new ResourceNotFoundException(userPath);
                    }
          }

          public boolean isRootDirectory(String objectName, long userId) {
                    return objectName.equals(USER_DIRECTORY.formatted(userId));
          }
}