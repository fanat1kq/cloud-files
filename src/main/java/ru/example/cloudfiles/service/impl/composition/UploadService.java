package ru.example.cloudfiles.service.impl.composition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.dto.Resource;
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


@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {
    private final S3Repository s3Repo;
    private final PathManager paths;
    private final MinioProperties props;
    private final ResourceMapper resourceMapper;

    public List<ResourceInfoResponseDTO> upload(long userId, String uploadPath, MultipartFile[] files) {

        validateUpload(userId, uploadPath, files);
        createDirs(userId, uploadPath, files);

        return Arrays.stream(files)
                .map(file -> uploadFile(userId, uploadPath, file))
                .map(resource -> resourceMapper.toDto(userId, resource))
                .toList();
    }

    private void validateUpload(long userId, String uploadPath, MultipartFile[] files) {

        if (!uploadPath.isBlank() && (!paths.isDirectory(uploadPath) || !resourceExists(userId, uploadPath))) {
            throw new DirectoryNotExistException(uploadPath);
        }

        Arrays.stream(files)
                .filter(file -> file.getOriginalFilename() != null)
                .filter(file -> resourceExists(userId, uploadPath + file.getOriginalFilename()))
                .findFirst()
                .ifPresent(file -> {
                    throw new ResourceAlreadyExistsException(file.getOriginalFilename());
                });
    }

    private void createDirs(long userId, String uploadPath, MultipartFile[] files) {
        Arrays.stream(files)
                .filter(file -> file.getOriginalFilename() != null)
                .flatMap(file -> extractDirs(uploadPath + file.getOriginalFilename()).stream())
                .filter(dir -> !resourceExists(userId, dir))
                .forEach(dir -> s3Repo.createDirectory(props.getBucket(), paths.toTechnicalPath(userId, dir)));
    }

    private Set<String> extractDirs(String fullPath) {

        Set<String> dirs = new HashSet<>();

        for (int i = 0; i < fullPath.length(); i++) {
            if (fullPath.charAt(i) == '/') dirs.add(fullPath.substring(0, i + 1));
        }

        if (paths.isDirectory(fullPath)) dirs.add(fullPath);

        return dirs;
    }

    private Resource uploadFile(long userId, String uploadPath, MultipartFile file) {

        String filename = Objects.requireNonNull(file.getOriginalFilename(), "Filename is null");
        String techPath = paths.toTechnicalPath(userId, uploadPath + filename);

        try (var is = file.getInputStream()) {
            s3Repo.saveResource(props.getBucket(), techPath, is);

            return s3Repo.getResourceByPath(props.getBucket(), techPath);
        } catch (Exception e) {
            throw new ResourceUploadException(filename, e);
        }
    }

    private boolean resourceExists(long userId, String path) {

        return s3Repo.isObjectExists(props.getBucket(), paths.toTechnicalPath(userId, path));
    }
}