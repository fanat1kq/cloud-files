package ru.example.cloudfiles.repository.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.example.cloudfiles.exception.StorageOperationImpl.directory.DirectoryCreationException;
import ru.example.cloudfiles.repository.DirectoryRepository;
import ru.example.cloudfiles.repository.ObjectRepository;
import ru.example.cloudfiles.validation.PathValidator;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MinioDirectoryRepository implements DirectoryRepository {

          private final MinioClient minioClient;
          private final PathValidator pathValidator;
          private final ObjectRepository objectRepository;

          @Override
          public void createDirectory(String bucketName, String path) {
                    pathValidator.validatePath(path);

                    List<String> directories = buildDirectoryHierarchy(path);

                    try {
                              for (String directory : directories) {
                                        if (!objectRepository.isObjectExists(bucketName, directory)) {
                                                  minioClient.putObject(PutObjectArgs.builder()
                                                            .bucket(bucketName)
                                                            .object(directory)
                                                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                                                            .build());
                                        }
                              }
                    } catch (Exception e) {
                              throw new DirectoryCreationException("Failed to create directory: " + path, e);
                    }
          }

          private List<String> buildDirectoryHierarchy(String path) {
                    List<String> directories = new ArrayList<>();

                    for (int i = 0; i < path.length(); i++) {
                              if (path.charAt(i) == '/') {
                                        directories.add(path.substring(0, i + 1));
                              }
                    }

                    String targetDirectory = path.endsWith("/") ? path : path + "/";
                    directories.add(targetDirectory);

                    return directories;
          }
}