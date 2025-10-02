package ru.example.cloudfiles.repository.impl;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.example.cloudfiles.exception.S3RepositoryException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DirectoryRepository {
          private final MinioClient minioClient;
          private final PathValidator pathValidator;
          private final ObjectRepository objectOps;

          public void createDirectory(String bucketName, String path) {
                    pathValidator.validatePath(path);

                    extractAllDirectories(path).stream()
                              .filter(dir -> !objectOps.isObjectExists(bucketName, dir))
                              .forEach(dir -> createEmptyObject(bucketName, dir));
          }

          private Set<String> extractAllDirectories(String path) {
                    Set<String> directories = new LinkedHashSet<>();

                    int lastSlash = 0;
                    while ((lastSlash = path.indexOf('/', lastSlash + 1)) != -1) {
                              directories.add(path.substring(0, lastSlash + 1));
                    }
                    directories.add(path);

                    return directories;
          }

          private void createEmptyObject(String bucketName, String path) {
                    try {
                              minioClient.putObject(PutObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(path)
                                        .stream(InputStream.nullInputStream(), 0, -1)
                                        .build());
                    } catch (Exception e) {
                              throw new S3RepositoryException("Failed to create directory: " + path, e);
                    }
          }

          public List<String> findAllNamesByPrefix(String bucket, String prefix, boolean recursive) {
                    try {
                              return StreamSupport.stream(
                                                  minioClient.listObjects(ListObjectsArgs.builder()
                                                            .bucket(bucket)
                                                            .prefix(prefix)
                                                            .recursive(recursive)
                                                            .build()).spliterator(), false)
                                        .map(this::extractObjectName)
                                        .toList();
                    } catch (Exception e) {
                              throw new S3RepositoryException("Failed to find", e);
                    }
          }

          private String extractObjectName(Result<Item> itemResult) {
                    try {
                              return itemResult.get().objectName();
                    } catch (Exception e) {
                              throw new S3RepositoryException("Failed to extract object name", e);
                    }
          }
}