package ru.example.cloudfiles.repository.impl;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.exception.storageOperation.directory.DirectoryDeletionException;
import ru.example.cloudfiles.exception.storageOperation.resource.ResourceDeletionException;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.repository.impl.composition.DirectoryRepository;
import ru.example.cloudfiles.repository.impl.composition.ObjectRepository;
import ru.example.cloudfiles.validation.PathValidator;

import java.io.InputStream;
import java.util.List;

@Component
@Slf4j
@ConditionalOnBean(MinioClient.class)
@RequiredArgsConstructor
public class MinioRepository implements S3Repository {

    private final MinioClient minioClient;
    private final ObjectRepository objectRepository;
    private final DirectoryRepository directoryRepository;
    private final PathValidator pathValidator;

    @Override
    public List<String> findAllNamesByPrefix(String bucket, String prefix, boolean recursive) {

        log.debug("Finding all names by prefix - bucket: {}, prefix: '{}', recursive: {}",
                bucket, prefix, recursive);
        return directoryRepository.findAllNamesByPrefix(bucket, prefix, recursive);
    }

    @Override
    public void deleteResource(String bucketName, String path) {

        pathValidator.validatePath(path);

        if (StringUtils.endsWithIgnoreCase(path, "/")) {
            log.trace("Deleting directory - path: '{}'", path);
            deleteDirectory(bucketName, path);
        } else {
            log.trace("Deleting single file - path: '{}'", path);
            deleteSingleObject(bucketName, path);
        }

        log.debug("Resource deleted successfully - bucket: {}, path: '{}'", bucketName, path);
    }

    private void deleteDirectory(String bucketName, String path) {

        try {
            List<String> objectNames = directoryRepository.findAllNamesByPrefix(bucketName, path, true);
            log.debug("Deleting directory - bucket: {}, path: '{}', objects: {}",
                    bucketName, path, objectNames.size());

            if (!objectNames.isEmpty()) {
                for (String objectName : objectNames) {
                    try {
                        minioClient.removeObject(
                                RemoveObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(objectName)
                                        .build());
                        log.trace("Directory object deleted - '{}'", objectName);
                    } catch (Exception e) {
                        log.error("Failed to delete directory object - '{}'", objectName, e);
                        throw new ResourceDeletionException(objectName, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to delete directory - bucket: {}, path: '{}'", bucketName, path, e);
            throw new DirectoryDeletionException(path, e);
        }
    }

    private void deleteSingleObject(String bucketName, String path) {

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
            log.trace("Single object deleted successfully - '{}'", path);
        } catch (Exception e) {
            log.error("Failed to delete single object - '{}'", path, e);
            throw new ResourceDeletionException(path, e);
        }
    }

    @Override
    public Resource getResourceByPath(String bucket, String path) {

        log.trace("Getting resource by path - bucket: {}, path: '{}'", bucket, path);
        return objectRepository.getResourceByPath(bucket, path);
    }

    @Override
    public void saveResource(String bucket, String path, InputStream dataStream) {

        log.trace("Saving resource - bucket: {}, path: '{}'", bucket, path);
        objectRepository.saveResource(bucket, path, dataStream);
    }

    @Override
    public void createDirectory(String bucketName, String path) {

        log.debug("Creating directory - bucket: {}, path: '{}'", bucketName, path);
        directoryRepository.createDirectory(bucketName, path);
    }

    @Override
    public boolean isObjectExists(String bucketName, String path) {

        log.trace("Checking object existence - bucket: {}, path: '{}'", bucketName, path);
        return objectRepository.isObjectExists(bucketName, path);
    }
}