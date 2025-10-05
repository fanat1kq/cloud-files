package ru.example.cloudfiles.service.impl.composition.fileOperations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.mapper.ResourceMapper;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileQueryService {

    private final S3Repository s3Repo;
    private final PathManager paths;
    private final MinioProperties props;
    private final ResourceMapper resourceMapper;

    public ResourceInfoResponseDTO getResource(long userId, String path) {

        try {
            var resource = s3Repo.getResourceByPath(props.getBucket(),
                    paths.toTechnicalPath(userId, path));
            return resourceMapper.toDto(userId, resource);
        } catch (Exception e) {
            throw new ResourceNotFoundException(path);
        }
    }

    public boolean resourceExists(long userId, String path) {

        return s3Repo.isObjectExists(props.getBucket(),
                paths.toTechnicalPath(userId, path));
    }

    public List<String> findAllNames(long userId, String prefix) {

        return s3Repo.findAllNamesByPrefix(props.getBucket(),
                paths.toTechnicalPath(userId, prefix), true);
    }
}
