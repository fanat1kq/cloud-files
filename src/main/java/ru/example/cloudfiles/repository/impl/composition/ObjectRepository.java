package ru.example.cloudfiles.repository.impl.composition;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.example.cloudfiles.entity.Resource;
import ru.example.cloudfiles.exception.storageOperationImpl.S3RepositoryException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceSaveException;

import java.io.InputStream;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ObjectRepository {

    private final MinioClient minioClient;
    private final PathValidator pathValidator;

    public Resource getResourceByPath(String bucket, String path) {

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

            return new Resource(path, objectStream, objectStat.size());
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new ResourceNotFoundException(path);
            }
            throw new ResourceNotFoundException(path);
        } catch (Exception e) {
            throw new S3RepositoryException("Repository exception for path", e);
        }
    }

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

    public boolean isObjectExists(String bucketName, String path) {

        pathValidator.validatePath(path);

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new S3RepositoryException("Repository exception", e);
        } catch (Exception e) {
            throw new S3RepositoryException("Object doesn`t exist", e);
        }
    }
}