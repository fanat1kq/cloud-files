package ru.example.cloudfiles.repository.impl.composition;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.exception.storageOperationImpl.S3RepositoryException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceSaveException;
import ru.example.cloudfiles.validation.PathValidator;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ObjectRepository {

    private final MinioClient minioClient;
    private final PathValidator pathValidator;

    public Resource getResourceByPath(String bucket, String path) {

        log.debug("Getting resource - bucket: {}, path: '{}'", bucket, path);

        pathValidator.validatePath(path);

        try {
            var objectStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());

            var objectStat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());

            log.trace("Resource retrieved - bucket: {}, path: '{}', size: {}", bucket, path, objectStat.size());
            return new Resource(path, objectStream, objectStat.size());
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                log.warn("Resource not found - bucket: {}, path: '{}'", bucket, path);
                throw new ResourceNotFoundException(path);
            }
            log.error("Error response for resource - bucket: {}, path: '{}', code: {}",
                    bucket, path, e.errorResponse().code(), e);
            throw new ResourceNotFoundException(path);
        } catch (Exception e) {
            log.error("Failed to get resource - bucket: {}, path: '{}'", bucket, path, e);
            throw new S3RepositoryException("Repository exception for path", e);
        }
    }

    public void saveResource(String bucket, String path, InputStream dataStream) {

        log.debug("Saving resource - bucket: {}, path: '{}'", bucket, path);

        pathValidator.validatePath(path);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(dataStream, -1, 10485760)
                    .build());
            log.trace("Resource saved successfully - bucket: {}, path: '{}'", bucket, path);
        } catch (Exception e) {
            log.error("Failed to save resource - bucket: {}, path: '{}'", bucket, path, e);
            throw new ResourceSaveException(path, e);
        }
    }

    public boolean isObjectExists(String bucketName, String path) {

        log.trace("Checking object existence - bucket: {}, path: '{}'", bucketName, path);

        pathValidator.validatePath(path);

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
            log.trace("Object exists - bucket: {}, path: '{}'", bucketName, path);
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                log.trace("Object not found - bucket: {}, path: '{}'", bucketName, path);
                return false;
            }
            log.error("Error response for object check - bucket: {}, path: '{}', code: {}",
                    bucketName, path, e.errorResponse().code(), e);
            throw new S3RepositoryException("Repository exception", e);
        } catch (Exception e) {
            log.error("Failed to check object existence - bucket: {}, path: '{}'", bucketName, path, e);
            throw new S3RepositoryException("Object doesn`t exist", e);
        }
    }
}