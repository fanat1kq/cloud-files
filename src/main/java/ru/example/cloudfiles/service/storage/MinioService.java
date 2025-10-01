package ru.example.cloudfiles.service.storage;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {

          private final MinioClient minioClient;

          private final String defaultBucketName;

          public MinioService(MinioClient minioClient,
                              @Value("${application.default-bucket-name}")
                              String defaultBucketName) {
                    this.minioClient = minioClient;
                    this.defaultBucketName = defaultBucketName;
          }


          public boolean objectExists(String objectName) throws Exception {
                    try {
                              minioClient.statObject(StatObjectArgs.builder()
                                        .bucket(defaultBucketName)
                                        .object(objectName)
                                        .build());
                              return true;
                    } catch (ErrorResponseException exception) {
                              return false;
                    }
          }

          public List<String> listAllObjects(String prefix) throws Exception {
                    List<String> objects = new ArrayList<>();
                    Iterable<Result<Item>> results =
                              minioClient.listObjects(ListObjectsArgs.builder()
                                        .bucket(defaultBucketName)
                                        .prefix(prefix)
                                        .recursive(true)
                                        .build());

                    for (Result<Item> result : results) {
                              Item item = result.get();
                              objects.add(item.objectName());
                    }
                    return objects;
          }

          public List<String> listObjects(String prefix, boolean recursive) throws Exception {
                    List<String> objects = new ArrayList<>();
                    Iterable<Result<Item>> results =
                              minioClient.listObjects(ListObjectsArgs.builder()
                                        .bucket(defaultBucketName)
                                        .prefix(prefix)
                                        .recursive(recursive)
                                        .build());

                    for (Result<Item> result : results) {
                              Item item = result.get();
                              objects.add(item.objectName());
                    }
                    return objects;
          }

          public void createDirectory(String directoryName) throws Exception {
                    minioClient.putObject(PutObjectArgs.builder()
                              .bucket(defaultBucketName)
                              .object(directoryName)
                              .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                              .build());
          }

          public void copyObject(String sourceObject, String destObject) throws Exception {
                    minioClient.copyObject(CopyObjectArgs.builder()
                              .bucket(defaultBucketName)
                              .object(destObject)
                              .source(CopySource.builder()
                                        .bucket(defaultBucketName)
                                        .object(sourceObject)
                                        .build())
                              .build());
          }

          public void deleteObject(String objectName) throws Exception {
                    minioClient.removeObject(RemoveObjectArgs.builder()
                              .bucket(defaultBucketName)
                              .object(objectName)
                              .build());
          }

          public InputStream getObjectStream(String objectName) throws Exception {
                    return minioClient.getObject(GetObjectArgs.builder()
                              .bucket(defaultBucketName)
                              .object(objectName)
                              .build());
          }

          public StatObjectResponse getObjectStats(String objectName) throws Exception {
                    return minioClient.statObject(StatObjectArgs.builder()
                              .bucket(defaultBucketName)
                              .object(objectName)
                              .build());
          }

          public void uploadFile(MultipartFile file, String objectName) throws Exception {
                    minioClient.putObject(PutObjectArgs.builder()
                              .bucket(defaultBucketName)
                              .object(objectName)
                              .stream(file.getInputStream(), file.getSize(), -1)
                              .contentType(file.getContentType())
                              .build());
          }
}