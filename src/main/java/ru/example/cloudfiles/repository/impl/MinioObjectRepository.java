package ru.example.cloudfiles.repository.impl;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.example.cloudfiles.entity.Resource;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceSaveException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceDeletionException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceExistenceCheckException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceListingException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceRetrievalException;
import ru.example.cloudfiles.repository.ObjectRepository;
import ru.example.cloudfiles.validation.PathValidator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MinioObjectRepository implements ObjectRepository {

          private final MinioClient minioClient;
          private final PathValidator pathValidator;

          @Override
          public void deleteResource(String bucketName, String path) {
                    pathValidator.validatePath(path);

                    try {
                              if (path.endsWith("/")) {
                                        deleteDirectoryRecursively(bucketName, path);
                              } else {
                                        deleteFile(bucketName, path);
                              }
                    } catch (Exception e) {
                              throw new ResourceDeletionException(path, e);
                    }
          }

          @Override
          public List<String> findAllNamesByPrefix(String bucket, String prefix, boolean recursive) {
                    try {
                              return listObjectsByPrefix(bucket, prefix, recursive);
                    } catch (Exception e) {
                              throw new ResourceListingException(prefix, e);
                    }
          }

          @Override
          public Resource getResourceByPath(String bucket, String path) {
                    try {
                              return fetchResource(bucket, path);
                    } catch (ErrorResponseException e) {
                              if (e.errorResponse().code().equals("NoSuchKey")) {
                                        throw new ResourceNotFoundException(path);
                              }
                              throw new ResourceRetrievalException(path, e);
                    } catch (Exception e) {
                              throw new ResourceRetrievalException(path, e);
                    }
          }

          @Override
          public void saveResource(String bucket, String path, InputStream dataStream) {
                    pathValidator.validatePath(path);

                    try {
                              minioClient.putObject(PutObjectArgs.builder()
                                        .bucket(bucket)
                                        .object(path)
                                        .stream(dataStream, -1, 10485760)
                                        .build());
                    } catch (Exception e) {
                              throw new ResourceSaveException(path, e);
                    }
          }

          @Override
          public boolean isObjectExists(String bucketName, String path) {
                    pathValidator.validatePath(path);

                    try {
                              minioClient.statObject(StatObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(path)
                                        .build());
                              return true;
                    } catch (ErrorResponseException e) {
                              if (e.errorResponse().code().equals("NoSuchKey")) {
                                        return false;
                              }
                              throw new ResourceExistenceCheckException(path, e);
                    } catch (Exception e) {
                              throw new ResourceExistenceCheckException(path, e);
                    }
          }

          private void deleteDirectoryRecursively(String bucketName, String directoryPath) throws Exception {
                    List<DeleteObject> deleteObjects = listObjectsForDeletion(bucketName, directoryPath);

                    if (!deleteObjects.isEmpty()) {
                              deleteObjectsBatch(bucketName, deleteObjects);
                    }
          }

          private List<DeleteObject> listObjectsForDeletion(String bucketName, String prefix) throws Exception {
                    var objects = minioClient.listObjects(ListObjectsArgs.builder()
                              .bucket(bucketName)
                              .prefix(prefix)
                              .recursive(true)
                              .build());

                    List<DeleteObject> deleteObjects = new ArrayList<>();
                    for (Result<Item> object : objects) {
                              deleteObjects.add(new DeleteObject(object.get().objectName()));
                    }
                    return deleteObjects;
          }

          private void deleteObjectsBatch(String bucketName, List<DeleteObject> deleteObjects) {
                    var result = minioClient.removeObjects(RemoveObjectsArgs.builder()
                              .bucket(bucketName)
                              .objects(deleteObjects)
                              .build());

                    consumeDeleteResults(result);
          }

          private void deleteFile(String bucketName, String path) throws Exception {
                    minioClient.removeObject(RemoveObjectArgs.builder()
                              .bucket(bucketName)
                              .object(path)
                              .build());
          }

          private List<String> listObjectsByPrefix(String bucket, String prefix, boolean recursive) throws Exception {
                    var objects = minioClient.listObjects(ListObjectsArgs.builder()
                              .bucket(bucket)
                              .prefix(prefix)
                              .recursive(recursive)
                              .build());

                    List<String> names = new ArrayList<>();
                    for (Result<Item> object : objects) {
                              names.add(object.get().objectName());
                    }
                    return names;
          }

          private Resource fetchResource(String bucket, String path) throws Exception {
                    var objectStream = minioClient.getObject(GetObjectArgs.builder()
                              .bucket(bucket)
                              .object(path)
                              .build());

                    var stat = minioClient.statObject(StatObjectArgs.builder()
                              .bucket(bucket)
                              .object(path)
                              .build());

                    return new Resource(objectStream.object(), objectStream, stat.size());
          }

          private void consumeDeleteResults(Iterable<Result<DeleteError>> results) {
                    for (Result<DeleteError> error : results) {
                              try {
                                        error.get();
                              } catch (Exception ignored) {
                              }
                    }
          }
}