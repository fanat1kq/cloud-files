package ru.example.cloudfiles.repository;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.example.cloudfiles.entity.Resource;
import ru.example.cloudfiles.exception.S3RepositoryException;
import ru.example.cloudfiles.repository.impl.DirectoryRepository;
import ru.example.cloudfiles.repository.impl.ObjectRepository;
import ru.example.cloudfiles.repository.impl.PathValidator;

import java.io.InputStream;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MinioRepository implements S3Repository {
    private final MinioClient minioClient;

    private final ObjectRepository objectRepository;

    private final DirectoryRepository directoryRepository;

    private final PathValidator pathValidator;

    @Override
    public List<String> findAllNamesByPrefix(String bucket, String prefix, boolean recursive) {
        return directoryRepository.findAllNamesByPrefix(bucket, prefix, recursive);
    }

    @Override
    public void deleteResource(String bucketName, String path) {
        pathValidator.validatePath(path);

        if (path.endsWith("/")) {
            deleteDirectory(bucketName, path);
        } else {
            deleteSingleObject(bucketName, path);
        }
    }

    private void deleteDirectory(String bucketName, String path) {
        try {
            List<String> objectNames = directoryRepository.findAllNamesByPrefix(bucketName, path, false);

            if (!objectNames.isEmpty()) {
                for (String objectName : objectNames) {
                    try {
                        minioClient.removeObject(
                                  RemoveObjectArgs.builder()
                                            .bucket(bucketName)
                                            .object(objectName)
                                            .build()
                        );
                    } catch (Exception e) {
                        throw new S3RepositoryException("Failed to delete object: " + objectName, e);
                    }
                }
            }
        } catch (Exception e) {
            throw new S3RepositoryException("Failed to delete directory: " + path, e);
        }
    }

    private void deleteSingleObject(String bucketName, String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                      .bucket(bucketName)
                      .object(path)
                      .build());
        } catch (Exception e) {
            throw new S3RepositoryException("Failed to delete object: " + path, e);
        }
    }

    @Override
    public Resource getResourceByPath(String bucket, String path) {
        return objectRepository.getResourceByPath(bucket, path);
    }

    @Override
    public void saveResource(String bucket, String path, InputStream dataStream) {
        objectRepository.saveResource(bucket, path, dataStream);
    }

    @Override
    public void createDirectory(String bucketName, String path) {
        directoryRepository.createDirectory(bucketName, path);
    }

    @Override
    public boolean isObjectExists(String bucketName, String path) {
        return objectRepository.isObjectExists(bucketName, path);
    }
}