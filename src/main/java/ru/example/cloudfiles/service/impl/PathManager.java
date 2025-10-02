package ru.example.cloudfiles.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.example.cloudfiles.config.properties.S3Properties;

@Component
@RequiredArgsConstructor
public class PathManager {

          private final S3Properties properties;

          public String getUserDirectory(long userId) {
                    return properties.getUserDirectoryPattern().formatted(userId);
          }

          public String toTechnicalPath(long userId, String userPath) {
                    return getUserDirectory(userId).concat(userPath);
          }

          public String toUserPath(long userId, String technicalPath) {
                    String userDirectory = getUserDirectory(userId);
                    return technicalPath.startsWith(userDirectory)
                              ? technicalPath.substring(userDirectory.length())
                              : technicalPath;
          }

          public boolean isDirectory(String path) {
                    return path.endsWith("/") && path.length() > 1;
          }

          public String ensureDirectoryPath(String path) {
                    return isDirectory(path) ? path : path + "/";
          }
}