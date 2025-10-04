package ru.example.cloudfiles.service.impl.composition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.exception.StorageOperationImpl.directory.NotDirectoryException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceAlreadyExistsException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.mapper.ResourceMapper;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectoryOperationsService {

    private final S3Repository s3Repo;
    private final PathManager paths;
    private final MinioProperties props;
    private final ResourceMapper resourceMapper;

    public void createUserDir(long userId) {
        log.debug("Creating user directory for userId: {}", userId);
        s3Repo.createDirectory(props.getBucket(), paths.getUserDirectory(userId));
    }

    public ResourceInfoResponseDTO createDir(long userId, String path) {

        log.debug("Creating directory for userId: {}, path: {}", userId, path);
        if (!paths.isDirectory(path)) throw new NotDirectoryException(path);
        if (resourceExists(userId, path)) throw new ResourceAlreadyExistsException(path);

        String techPath = paths.toTechnicalPath(userId, path);
        s3Repo.createDirectory(props.getBucket(), techPath);
        return resourceMapper.toDto(userId, s3Repo.getResourceByPath(props.getBucket(), techPath));
    }

    public List<ResourceInfoResponseDTO> getDir(long userId, String path) {

        log.debug("Getting directory contents for userId: {}, path: {}", userId, path);
        if (!paths.isDirectory(path) && !path.isBlank()) throw new NotDirectoryException(path);
        if (!path.isBlank() && !resourceExists(userId, path)) throw new ResourceNotFoundException(path);

        String userDir = paths.getUserDirectory(userId);
        String techPath = paths.toTechnicalPath(userId, path);

        return findAllNames(userId, path, false).stream()
                .filter(name -> !name.equals(userDir) && (path.isBlank() || !name.equals(techPath)))
                .map(name -> resourceMapper.toDto(userId, s3Repo.getResourceByPath(props.getBucket(), name)))
                .toList();
    }

    private boolean resourceExists(long userId, String path) {

        return s3Repo.isObjectExists(props.getBucket(), paths.toTechnicalPath(userId, path));
    }

    private List<String> findAllNames(long userId, String prefix, boolean recursive) {

        return s3Repo.findAllNamesByPrefix(props.getBucket(), paths.toTechnicalPath(userId, prefix), recursive);
    }
}