package ru.example.cloudfiles.repository.impl;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Repository;
import ru.example.cloudfiles.exception.StorageOperationImpl.bucket.BucketCreationException;
import ru.example.cloudfiles.exception.StorageOperationImpl.bucket.BucketDeletionException;
import ru.example.cloudfiles.repository.BucketRepository;

import java.util.Objects;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class MinioBucketRepository implements BucketRepository {

          private final MinioClient minioClient;

          @Override
          public void deleteBucket(String bucketName) {
                    try {
                              deleteAllObjectsInBucket(bucketName);
                              minioClient.removeBucket(
                                        RemoveBucketArgs.builder().bucket(bucketName).build());
                    } catch (Exception e) {
                              throw new BucketDeletionException(bucketName, e);
                    }
          }

          private void deleteAllObjectsInBucket(String bucketName) {
                    try {
                              var objects = minioClient.listObjects(ListObjectsArgs.builder()
                                        .bucket(bucketName)
                                        .recursive(true)
                                        .build());

                              var deleteObjects = StreamSupport.stream(objects.spliterator(), false)
                                        .map(this::getObjectName)
                                        .filter(Objects::nonNull)
                                        .map(DeleteObject::new)
                                        .toList();

                              if (!deleteObjects.isEmpty()) {
                                        var result = minioClient.removeObjects(
                                                  RemoveObjectsArgs.builder()
                                                            .bucket(bucketName)
                                                            .objects(deleteObjects)
                                                            .build());

                                        processDeleteResults(result);
                              }
                    } catch (Exception e) {
                              throw new BucketDeletionException(
                                        "Failed to delete objects in bucket: " + bucketName, e);
                    }
          }

          @SneakyThrows
          private String getObjectName(Result<Item> itemResult) {

                    return itemResult.get().objectName();

          }

          private void processDeleteResults(Iterable<Result<DeleteError>> results) {
                    StreamSupport.stream(results.spliterator(), false)
                              .forEach(this::processDeleteError);
          }

          @SneakyThrows
          private void processDeleteError(Result<DeleteError> errorResult) {
                              errorResult.get();
          }

          @Override
          public boolean isBucketExists(String bucketName) {
                    try {
                              return minioClient.bucketExists(
                                        BucketExistsArgs.builder().bucket(bucketName).build());
                    } catch (Exception e) {
                              return false;
                    }
          }
}