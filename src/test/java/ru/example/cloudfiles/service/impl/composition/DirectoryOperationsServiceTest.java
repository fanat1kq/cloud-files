package ru.example.cloudfiles.service.impl.composition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.exception.storageOperationImpl.directory.NotDirectoryException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceAlreadyExistsException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.mapper.ResourceMapper;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DirectoryOperationsServiceTest {

    @Mock
    private S3Repository s3Repo;

    @Mock
    private PathManager paths;

    @Mock
    private MinioProperties props;

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private DirectoryOperationsService directoryOperationsService;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Should create user directory")
    void createUserDirectoryOk() {

        long userId = factory.manufacturePojo(Long.class);
        String userDirectory = factory.manufacturePojo(String.class);
        String bucket = factory.manufacturePojo(String.class);

        when(paths.getUserDirectory(userId)).thenReturn(userDirectory);
        when(props.getBucket()).thenReturn(bucket);

        directoryOperationsService.createUserDir(userId);

        verify(s3Repo).createDirectory(bucket, userDirectory);
        verify(paths).getUserDirectory(userId);
        verify(props).getBucket();
    }

    @Test
    @DisplayName("Should create directory successfully")
    void createDirectoryOk() {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class) + "/";
        String techPath = factory.manufacturePojo(String.class);
        String bucket = factory.manufacturePojo(String.class);
        Resource resource = factory.manufacturePojo(Resource.class);
        ResourceInfoResponseDTO expectedDto = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(paths.isDirectory(path)).thenReturn(true);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(props.getBucket()).thenReturn(bucket);
        when(s3Repo.isObjectExists(bucket, techPath)).thenReturn(false);
        when(s3Repo.getResourceByPath(bucket, techPath)).thenReturn(resource);
        when(resourceMapper.toDto(userId, resource)).thenReturn(expectedDto);

        ResourceInfoResponseDTO result = directoryOperationsService.createDir(userId, path);

        assertEquals(expectedDto, result);
        verify(s3Repo).createDirectory(bucket, techPath);
        verify(s3Repo).isObjectExists(bucket, techPath);
        verify(s3Repo).getResourceByPath(bucket, techPath);
        verify(resourceMapper).toDto(userId, resource);
    }

    @Test
    @DisplayName("Should throw NotDirectoryException when path is not a directory")
    void createDirectoryNotDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class);

        when(paths.isDirectory(path)).thenReturn(false);

        assertThrows(NotDirectoryException.class, () -> directoryOperationsService.createDir(userId, path));
        verify(s3Repo, never()).createDirectory(any(), any());
    }

    @Test
    @DisplayName("Should throw ResourceAlreadyExistsException when resource already exists")
    void createDirectoryResourceAlreadyExists() {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class) + "/";
        String techPath = factory.manufacturePojo(String.class);
        String bucket = factory.manufacturePojo(String.class);

        when(paths.isDirectory(path)).thenReturn(true);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(props.getBucket()).thenReturn(bucket);
        when(s3Repo.isObjectExists(bucket, techPath)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> directoryOperationsService.createDir(userId, path));
        verify(s3Repo, never()).createDirectory(any(), any());
    }

    @Test
    @DisplayName("Should get directory contents for nested path")
    void getDirectoryNestedPath() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/";
        String userDir = factory.manufacturePojo(String.class);
        String techPath = factory.manufacturePojo(String.class);
        String bucket = factory.manufacturePojo(String.class);
        List<String> names = List.of(
                techPath,
                techPath + "file1.pdf",
                techPath + "file2.doc"
        );

        List<ResourceInfoResponseDTO> expectedDtos = new ArrayList<>();
        for (int i = 0; i < names.size() - 1; i++) {
            expectedDtos.add(factory.manufacturePojo(ResourceInfoResponseDTO.class));
        }

        when(paths.isDirectory(path)).thenReturn(true);
        when(paths.getUserDirectory(userId)).thenReturn(userDir);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(props.getBucket()).thenReturn(bucket);
        when(s3Repo.isObjectExists(bucket, techPath)).thenReturn(true);
        when(s3Repo.findAllNamesByPrefix(bucket, techPath, false)).thenReturn(names);

        for (int i = 1; i < names.size(); i++) {
            Resource resource = factory.manufacturePojo(Resource.class);
            when(s3Repo.getResourceByPath(bucket, names.get(i))).thenReturn(resource);
            when(resourceMapper.toDto(userId, resource)).thenReturn(expectedDtos.get(i - 1));
        }

        List<ResourceInfoResponseDTO> result = directoryOperationsService.getDir(userId, path);

        assertEquals(expectedDtos.size(), result.size());
        verify(s3Repo).findAllNamesByPrefix(bucket, techPath, false);
    }

    @Test
    @DisplayName("Should throw NotDirectoryException when path is not a directory")
    void getDirectoryNotDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class);

        when(paths.isDirectory(path)).thenReturn(false);

        assertThrows(NotDirectoryException.class, () -> directoryOperationsService.getDir(userId, path));
        verify(s3Repo, never()).findAllNamesByPrefix(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when path does not exist")
    void getDirectoryResourceNotFound() {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class) + "/";
        String techPath = factory.manufacturePojo(String.class);
        String bucket = factory.manufacturePojo(String.class);

        when(paths.isDirectory(path)).thenReturn(true);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(props.getBucket()).thenReturn(bucket);
        when(s3Repo.isObjectExists(bucket, techPath)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> directoryOperationsService.getDir(userId, path));
        verify(s3Repo, never()).findAllNamesByPrefix(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Should filter out user directory and current directory from results")
    void getDirectoryFiltersCorrectly() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/";
        String userDir = factory.manufacturePojo(String.class);
        String techPath = userDir + path;
        String bucket = factory.manufacturePojo(String.class);
        List<String> names = List.of(
                userDir,
                techPath,
                techPath + "file1.pdf",
                techPath + "subdir/"
        );

        List<ResourceInfoResponseDTO> expectedDtos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            expectedDtos.add(factory.manufacturePojo(ResourceInfoResponseDTO.class));
        }

        when(paths.isDirectory(path)).thenReturn(true);
        when(paths.getUserDirectory(userId)).thenReturn(userDir);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(props.getBucket()).thenReturn(bucket);
        when(s3Repo.isObjectExists(bucket, techPath)).thenReturn(true);
        when(s3Repo.findAllNamesByPrefix(bucket, techPath, false)).thenReturn(names);

        for (int i = 2; i < names.size(); i++) {
            Resource resource = factory.manufacturePojo(Resource.class);
            when(s3Repo.getResourceByPath(bucket, names.get(i))).thenReturn(resource);
            when(resourceMapper.toDto(userId, resource)).thenReturn(expectedDtos.get(i - 2));
        }

        List<ResourceInfoResponseDTO> result = directoryOperationsService.getDir(userId, path);

        assertEquals(2, result.size());

        verify(s3Repo, times(2)).getResourceByPath(eq(bucket), anyString());

        verify(s3Repo).getResourceByPath(bucket, techPath + "file1.pdf");
        verify(s3Repo).getResourceByPath(bucket, techPath + "subdir/");
    }
}