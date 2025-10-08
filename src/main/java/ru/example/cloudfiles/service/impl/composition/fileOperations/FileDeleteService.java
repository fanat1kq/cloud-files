package ru.example.cloudfiles.service.impl.composition.fileOperations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileDeleteService {

    private final S3Repository s3Repo;
    private final FileQueryService fileQueryService;
    private final PathManager paths;
    private final MinioProperties props;

    public void deleteResource(long userId, String path) {

        log.info("Delete started - userId: {}, path: '{}'", userId, path);

        if (!fileQueryService.resourceExists(userId, path)) {
            log.warn("Resource not found for deletion - userId: {}, path: '{}'", userId, path);
            throw new ResourceNotFoundException(path);
        }

        s3Repo.deleteResource(props.getBucket(), paths.toTechnicalPath(userId, path));
        log.info("Delete completed - userId: {}, path: '{}'", userId, path);
    }
}