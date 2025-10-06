package ru.example.cloudfiles.service.impl.composition.fileOperations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceAlreadyExistsException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileMoveService {
    private final S3Repository s3Repo;
    private final FileQueryService fileQueryService;
    private final PathManager paths;
    private final MinioProperties props;

    public ResourceInfoResponseDTO moveResource(long userId, String oldPath, String newPath) {

        validateMove(userId, oldPath, newPath);

        String oldTech = paths.toTechnicalPath(userId, oldPath);
        String newTech = paths.toTechnicalPath(userId, newPath);

        if (paths.isDirectory(oldPath)) {
            moveDir(oldTech, newTech);
        } else {
            moveFile(oldTech, newTech);
        }

        s3Repo.deleteResource(props.getBucket(), oldTech);
        return fileQueryService.getResource(userId, newPath);
    }

    private void moveDir(String oldTech, String newTech) {

        String oldPrefix = oldTech.endsWith("/") ? oldTech : oldTech + "/";
        String newPrefix = newTech.endsWith("/") ? newTech : newTech + "/";

        var sourcePaths = s3Repo.findAllNamesByPrefix(props.getBucket(), oldPrefix, true);

        sourcePaths.forEach(oldName -> {
            String newName = newPrefix + oldName.substring(oldPrefix.length());
            var resource = s3Repo.getResourceByPath(props.getBucket(), oldName);
            try (var is = resource.dataStream()) {
                s3Repo.saveResource(props.getBucket(), newName, new ByteArrayInputStream(is.readAllBytes()));
            } catch (IOException e) {
                throw new ResourceNotFoundException("Failed to read resource: " + oldName);
            }
        });

        sourcePaths.forEach(oldName -> s3Repo.deleteResource(props.getBucket(), oldName));
    }

    private void moveFile(String oldTech, String newTech) {

        var resource = s3Repo.getResourceByPath(props.getBucket(), oldTech);
        s3Repo.saveResource(props.getBucket(), newTech, resource.dataStream());
    }

    private void validateMove(long userId, String oldPath, String newPath) {

        if (!fileQueryService.resourceExists(userId, oldPath))
            throw new ResourceNotFoundException(oldPath);
        if (fileQueryService.resourceExists(userId, newPath))
            throw new ResourceAlreadyExistsException(newPath);
    }
}