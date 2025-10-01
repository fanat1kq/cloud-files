package ru.example.cloudfiles.service.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceDeletionException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.StorageOperationException;
import ru.example.cloudfiles.service.storage.MinioService;
import ru.example.cloudfiles.util.FilePathUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileOperationsManager {

          private final MinioService minioService;
          private final FilePathUtils pathUtils;

          public void move(String source, String destination) throws Exception {
                    if (pathUtils.isDirectory(source)) {
                              moveDirectory(source, destination);
                    } else {
                              moveFile(source, destination);
                    }
          }

          public void delete(String path) throws Exception {
                    if (pathUtils.isDirectory(path)) {
                              deleteDirectory(path);
                    } else {
                              minioService.deleteObject(path);
                    }
          }

          public void createDirectory(String path) throws Exception {
                    minioService.createDirectory(path);
          }

          public Set<String> collectParentDirectories(MultipartFile[] files, String basePath) {
                    return Arrays.stream(files)
                              .map(MultipartFile::getOriginalFilename)
                              .filter(Objects::nonNull)
                              .flatMap(filename -> collectDirectoryPaths(filename, basePath).stream())
                              .collect(Collectors.toSet());
          }

          private void moveDirectory(String source, String destination) throws Exception {
                    minioService.listAllObjects(source)
                              .forEach(oldName -> {
                                        String newName = oldName.replace(source, destination);
                                        try {
                                                  minioService.copyObject(oldName, newName);
                                                  minioService.deleteObject(oldName);
                                        } catch (Exception e) {
                                                  throw new StorageOperationException("Failed to move directory from %s to %s".formatted(source, destination), e);
                                        }
                              });
          }

          private void moveFile(String source, String destination) throws Exception {
                    minioService.copyObject(source, destination);
                    minioService.deleteObject(source);
          }

          private void deleteDirectory(String path) throws Exception {
                    minioService.listAllObjects(path)
                              .forEach(objectName -> {
                                        try {
                                                  minioService.deleteObject(objectName);
                                        } catch (Exception e) {
                                                  throw new ResourceDeletionException(objectName, e);
                                        }
                              });
          }

          private Set<String> collectDirectoryPaths(String fileName, String basePath) {
                    Set<String> directories = new HashSet<>();
                    StringBuilder currentPath = new StringBuilder();
                    String[] parts = fileName.split("/");

                    for (int i = 0; i < parts.length - 1; i++) {
                              currentPath.append(parts[i]).append("/");
                              directories.add(basePath + currentPath);
                    }
                    return directories;
          }
}