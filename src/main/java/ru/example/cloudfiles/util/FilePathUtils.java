package ru.example.cloudfiles.util;

import org.springframework.stereotype.Component;
import ru.example.cloudfiles.mapper.model.PathInfo;

@Component
public class FilePathUtils {

          private static final int BUFFER_SIZE = 8192;

          public static PathInfo extractPathInfo(String relativePath, boolean isDirectory) {
                    String cleanPath = relativePath.endsWith("/") ?
                              relativePath.substring(0, relativePath.length() - 1) : relativePath;

                    int lastSlash = cleanPath.lastIndexOf("/");

                    if (lastSlash != -1) {
                              String fileName = cleanPath.substring(lastSlash + 1);
                              String path = cleanPath.substring(0, lastSlash + 1);
                              return new PathInfo(path, isDirectory ? fileName + "/" : fileName);
                    } else {
                              return new PathInfo("", isDirectory ? cleanPath + "/" : cleanPath);
                    }
          }

          public String normalizePath(String path) {
                    if (path == null || path.isBlank()) {
                              return "";
                    }

                    path = path.replace("//", "/");

                    if (path.startsWith("/")) {
                              path = path.substring(1);
                    }

                    return isDirectory(path) ? normalizeDirectoryPath(path) : path;
          }

          public String normalizeDirectoryPath(String path) {
                    if (path == null || path.isBlank()) {
                              return "";
                    }

                    path = path.replace("//", "/");

                    if (path.startsWith("/")) {
                              path = path.substring(1);
                    }

                    return path.endsWith("/") ? path : path + "/";
          }

          public boolean isDirectory(String path) {
                    return path != null && path.endsWith("/");
          }


          public int getBufferSize() {
                    return BUFFER_SIZE;
          }
}