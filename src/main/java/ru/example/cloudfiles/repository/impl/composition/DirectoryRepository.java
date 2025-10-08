package ru.example.cloudfiles.repository.impl.composition;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.example.cloudfiles.exception.storageOperationImpl.S3RepositoryException;
import ru.example.cloudfiles.exception.storageOperationImpl.directory.DirectoryCreationException;
import ru.example.cloudfiles.exception.storageOperationImpl.directory.DirectoryNotExistException;
import ru.example.cloudfiles.validation.PathValidator;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class DirectoryRepository {

    private final MinioClient minioClient;
    private final PathValidator pathValidator;
    private final ObjectRepository objectOps;

    public void createDirectory(String bucketName, String path) {

        log.debug("Creating directory - bucket: {}, path: '{}'", bucketName, path);

        pathValidator.validatePath(path);

        Set<String> directories = extractAllDirectories(path);
        log.trace("Extracted {} directories to create", directories.size());

        directories.stream()
                .filter(dir -> !objectOps.isObjectExists(bucketName, dir))
                .forEach(dir -> createEmptyObject(bucketName, dir));

        log.debug("Directory creation completed - path: '{}'", path);
    }

    private Set<String> extractAllDirectories(String path) {

        Set<String> directories = ConcurrentHashMap.newKeySet();

        IntStream.range(0, path.length())
                .filter(i -> path.charAt(i) == '/')
                .mapToObj(i -> path.substring(0, i + 1))
                .forEach(directories::add);

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
            log.trace("Empty object created - bucket: {}, path: '{}'", bucketName, path);
        } catch (Exception e) {
            log.error("Failed to create empty object - bucket: {}, path: '{}'", bucketName, path, e);
            throw new DirectoryCreationException(path, e);
        }
    }

    public List<String> findAllNamesByPrefix(String bucket, String prefix, boolean recursive) {

        log.debug("Finding names by prefix - bucket: {}, prefix: '{}', recursive: {}",
                bucket, prefix, recursive);

        try {
            List<String> names = StreamSupport.stream(
                            minioClient.listObjects(ListObjectsArgs.builder()
                                    .bucket(bucket)
                                    .prefix(prefix)
                                    .recursive(recursive)
                                    .build()).spliterator(), false)
                    .map(this::extractObjectName)
                    .toList();

            log.debug("Found {} names for prefix: '{}'", names.size(), prefix);
            return names;
        } catch (Exception e) {
            log.error("Failed to find names by prefix - bucket: {}, prefix: '{}'", bucket, prefix, e);
            throw new DirectoryNotExistException(prefix);
        }
    }

    private String extractObjectName(Result<Item> itemResult) {

        try {
            return itemResult.get().objectName();
        } catch (Exception e) {
            log.error("Failed to extract object name from result", e);
            throw new S3RepositoryException("Failed to extract object name", e);
        }
    }
}