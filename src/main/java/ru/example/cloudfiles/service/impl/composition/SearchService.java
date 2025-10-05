package ru.example.cloudfiles.service.impl.composition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.entity.Resource;
import ru.example.cloudfiles.exception.validation.InvalidSearchQueryException;
import ru.example.cloudfiles.mapper.ResourceMapper;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final S3Repository s3Repo;
    private final PathManager paths;
    private final MinioProperties props;
    private final ResourceMapper resourceMapper;

    public List<ResourceInfoResponseDTO> search(long userId, String query) {

        String searchQuery = validateAndNormalizeQuery(query);

        return findAllNames(userId, "", true).stream()
                .map(name -> paths.toUserPath(userId, name))
                .filter(userPath -> userPath.toLowerCase().contains(searchQuery))
                .map(userPath -> getResourceInternal(userId, userPath))
                .map(resource -> resourceMapper.toDto(userId, resource))
                .toList();
    }

    private Resource getResourceInternal(long userId, String path) {

        return s3Repo.getResourceByPath(props.getBucket(), paths.toTechnicalPath(userId, path));
    }

    private List<String> findAllNames(long userId, String prefix, boolean recursive) {

        return s3Repo.findAllNamesByPrefix(props.getBucket(),
                paths.toTechnicalPath(userId, prefix), recursive);
    }

    private String validateAndNormalizeQuery(String query) {

        if (query == null || query.trim().isEmpty()) {
            throw new InvalidSearchQueryException("Search query cannot be null or empty");
        }
        return query.toLowerCase().trim();
    }
}
