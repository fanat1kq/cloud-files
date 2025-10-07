package ru.example.cloudfiles.repository.impl;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.exception.storageOperationImpl.directory.DirectoryDeletionException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceDeletionException;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.repository.impl.composition.DirectoryRepository;
import ru.example.cloudfiles.repository.impl.composition.ObjectRepository;
import ru.example.cloudfiles.validation.PathValidator;

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

        if (StringUtils.endsWithIgnoreCase(path, "/")) {
            deleteDirectory(bucketName, path);
        } else {
            deleteSingleObject(bucketName, path);
        }
    }

    private void deleteDirectory(String bucketName, String path) {

        try {
            List<String> objectNames = directoryRepository.findAllNamesByPrefix(bucketName, path, true);

            if (!objectNames.isEmpty()) {
                for (String objectName : objectNames) {
                    try {
                        minioClient.removeObject(
                                RemoveObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(objectName)
                                        .build());
                    } catch (Exception e) {
                        throw new ResourceDeletionException(objectName, e);
                    }
                }
            }
        } catch (Exception e) {
            throw new DirectoryDeletionException(path, e);
        }
    }

    private void deleteSingleObject(String bucketName, String path) {

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new ResourceDeletionException(path, e);
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