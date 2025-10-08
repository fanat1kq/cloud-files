package ru.example.cloudfiles.service.impl.composition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.exception.storageOperationImpl.directory.DirectoryNotExistException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceAlreadyExistsException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceUploadException;
import ru.example.cloudfiles.mapper.ResourceMapper;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;


@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final S3Repository s3Repo;
    private final PathManager paths;
    private final MinioProperties props;
    private final ResourceMapper resourceMapper;

    public List<ResourceInfoResponseDTO> upload(long userId, String uploadPath, MultipartFile[] files) {

        log.info("Upload started - userId: {}, path: '{}', files: {}", userId, uploadPath, files.length);

        validateUpload(userId, uploadPath, files);
        createDirs(userId, uploadPath, files);

        List<ResourceInfoResponseDTO> results = Arrays.stream(files)
                .map(file -> uploadFile(userId, uploadPath, file))
                .map(resource -> resourceMapper.toDto(userId, resource))
                .toList();

        log.info("Upload completed - userId: {}, uploaded: {}", userId, results.size());
        return results;
    }

    private void validateUpload(long userId, String uploadPath, MultipartFile[] files) {

        if (!uploadPath.isBlank() && (!paths.isDirectory(uploadPath) || !resourceExists(userId, uploadPath))) {
            log.warn("Upload directory not found - userId: {}, path: '{}'", userId, uploadPath);
            throw new DirectoryNotExistException(uploadPath);
        }

        Arrays.stream(files)
                .filter(file -> StringUtils.isNotBlank(file.getOriginalFilename()))
                .filter(file -> resourceExists(userId, uploadPath + file.getOriginalFilename()))
                .findFirst()
                .ifPresent(file -> {
                    log.warn("Resource already exists - userId: {}, file: '{}'", userId, file.getOriginalFilename());
                    throw new ResourceAlreadyExistsException(file.getOriginalFilename());
                });
    }

    private void createDirs(long userId, String uploadPath, MultipartFile[] files) {

        long createdDirs = Arrays.stream(files)
                .filter(file -> StringUtils.isNotBlank(file.getOriginalFilename()))
                .flatMap(file -> extractDirs(uploadPath + file.getOriginalFilename()).stream())
                .filter(dir -> !resourceExists(userId, dir))
                .count();

        if (createdDirs > 0) {
            log.debug("Created {} directories for upload", createdDirs);
        }
    }

    private Set<String> extractDirs(String fullPath) {

        Set<String> dirs = new HashSet<>();

        IntStream.range(0, fullPath.length())
                .filter(i -> fullPath.charAt(i) == '/')
                .mapToObj(i -> fullPath.substring(0, i + 1))
                .forEach(dirs::add);

        if (paths.isDirectory(fullPath)) {
            dirs.add(fullPath);
        }

        return dirs;
    }

    private Resource uploadFile(long userId, String uploadPath, MultipartFile file) {

        String filename = Objects.requireNonNull(file.getOriginalFilename(), "Filename is null");
        String techPath = paths.toTechnicalPath(userId, uploadPath + filename);

        try (var is = file.getInputStream()) {
            s3Repo.saveResource(props.getBucket(), techPath, is);
            log.debug("File uploaded successfully - userId: {}, file: '{}'", userId, filename);

            return s3Repo.getResourceByPath(props.getBucket(), techPath);
        } catch (Exception e) {
            log.error("File upload failed - userId: {}, file: '{}'", userId, filename, e);
            throw new ResourceUploadException(filename, e);
        }
    }

    private boolean resourceExists(long userId, String path) {

        return s3Repo.isObjectExists(props.getBucket(), paths.toTechnicalPath(userId, path));
    }
}