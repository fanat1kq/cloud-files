package ru.example.cloudfiles.service.impl.composition.fileOperations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.mapper.ResourceMapper;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileQueryServiceTest {

    @Mock
    private S3Repository s3Repo;

    @Mock
    private PathManager paths;

    @Mock
    private MinioProperties props;

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private FileQueryService fileQueryService;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Should get resource successfully")
    void getResourceOk() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/file.txt";
        String techPath = "user-" + userId + "/documents/file.txt";
        String bucket = factory.manufacturePojo(String.class);

        Resource resource = factory.manufacturePojo(Resource.class);
        ResourceInfoResponseDTO expectedDto = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(s3Repo.getResourceByPath(bucket, techPath)).thenReturn(resource);
        when(resourceMapper.toDto(userId, resource)).thenReturn(expectedDto);

        ResourceInfoResponseDTO result = fileQueryService.getResource(userId, path);

        assertEquals(expectedDto, result);
        verify(s3Repo).getResourceByPath(bucket, techPath);
        verify(resourceMapper).toDto(userId, resource);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when get resource fails")
    void getResourceNotFound() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "nonexistent/file.txt";
        String techPath = "user-" + userId + "/nonexistent/file.txt";
        String bucket = factory.manufacturePojo(String.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(s3Repo.getResourceByPath(bucket, techPath)).thenThrow(new RuntimeException("Not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> fileQueryService.getResource(userId, path));

        verify(s3Repo).getResourceByPath(bucket, techPath);
        verify(resourceMapper, never()).toDto(anyLong(), any(Resource.class));
    }

    @Test
    @DisplayName("Should return true when resource exists")
    void resourceExistsTrue() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/file.txt";
        String techPath = "user-" + userId + "/documents/file.txt";
        String bucket = factory.manufacturePojo(String.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(s3Repo.isObjectExists(bucket, techPath)).thenReturn(true);

        boolean result = fileQueryService.resourceExists(userId, path);

        assertTrue(result);
        verify(s3Repo).isObjectExists(bucket, techPath);
    }

    @Test
    @DisplayName("Should return false when resource does not exist")
    void resourceExistsFalse() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "nonexistent/file.txt";
        String techPath = "user-" + userId + "/nonexistent/file.txt";
        String bucket = factory.manufacturePojo(String.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(s3Repo.isObjectExists(bucket, techPath)).thenReturn(false);

        boolean result = fileQueryService.resourceExists(userId, path);

        assertFalse(result);
        verify(s3Repo).isObjectExists(bucket, techPath);
    }

    @Test
    @DisplayName("Should find all names with prefix")
    void findAllNamesOk() {

        long userId = factory.manufacturePojo(Long.class);
        String prefix = "documents/";
        String techPrefix = "user-" + userId + "/documents/";
        String bucket = factory.manufacturePojo(String.class);

        List<String> expectedNames = List.of(
                "user-" + userId + "/documents/file1.txt",
                "user-" + userId + "/documents/file2.txt"
        );

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, prefix)).thenReturn(techPrefix);
        when(s3Repo.findAllNamesByPrefix(bucket, techPrefix, true)).thenReturn(expectedNames);

        List<String> result = fileQueryService.findAllNames(userId, prefix);

        assertEquals(expectedNames, result);
        verify(s3Repo).findAllNamesByPrefix(bucket, techPrefix, true);
    }

    @Test
    @DisplayName("Should return empty list when no resources found")
    void findAllNamesEmpty() {

        long userId = factory.manufacturePojo(Long.class);
        String prefix = "empty/";
        String techPrefix = "user-" + userId + "/empty/";
        String bucket = factory.manufacturePojo(String.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, prefix)).thenReturn(techPrefix);
        when(s3Repo.findAllNamesByPrefix(bucket, techPrefix, true)).thenReturn(List.of());

        List<String> result = fileQueryService.findAllNames(userId, prefix);

        assertTrue(result.isEmpty());
        verify(s3Repo).findAllNamesByPrefix(bucket, techPrefix, true);
    }

    @Test
    @DisplayName("Should handle file path for resource exists")
    void resourceExistsFile() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "file.txt";
        String techPath = "user-" + userId + "/file.txt";
        String bucket = factory.manufacturePojo(String.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(s3Repo.isObjectExists(bucket, techPath)).thenReturn(true);

        boolean result = fileQueryService.resourceExists(userId, path);

        assertTrue(result);
        verify(paths).toTechnicalPath(userId, path);
    }

    @Test
    @DisplayName("Should handle directory path for resource exists")
    void resourceExistsDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/";
        String techPath = "user-" + userId + "/documents/";
        String bucket = factory.manufacturePojo(String.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(s3Repo.isObjectExists(bucket, techPath)).thenReturn(true);

        boolean result = fileQueryService.resourceExists(userId, path);

        assertTrue(result);
        verify(paths).toTechnicalPath(userId, path);
    }

    @Test
    @DisplayName("Should handle root prefix for findAllNames")
    void findAllNamesRoot() {

        long userId = factory.manufacturePojo(Long.class);
        String prefix = "";
        String techPrefix = "user-" + userId + "/";
        String bucket = factory.manufacturePojo(String.class);

        List<String> expectedNames = List.of(
                "user-" + userId + "/file1.txt",
                "user-" + userId + "/documents/file2.txt"
        );

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, prefix)).thenReturn(techPrefix);
        when(s3Repo.findAllNamesByPrefix(bucket, techPrefix, true)).thenReturn(expectedNames);

        List<String> result = fileQueryService.findAllNames(userId, prefix);

        assertEquals(expectedNames, result);
        verify(s3Repo).findAllNamesByPrefix(bucket, techPrefix, true);
    }

    @Test
    @DisplayName("Should handle nested directory for findAllNames")
    void findAllNamesNested() {

        long userId = factory.manufacturePojo(Long.class);
        String prefix = "projects/src/main/";
        String techPrefix = "user-" + userId + "/projects/src/main/";
        String bucket = factory.manufacturePojo(String.class);

        List<String> expectedNames = List.of(
                "user-" + userId + "/projects/src/main/java/",
                "user-" + userId + "/projects/src/main/resources/"
        );

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, prefix)).thenReturn(techPrefix);
        when(s3Repo.findAllNamesByPrefix(bucket, techPrefix, true)).thenReturn(expectedNames);

        List<String> result = fileQueryService.findAllNames(userId, prefix);

        assertEquals(expectedNames, result);
        verify(s3Repo).findAllNamesByPrefix(bucket, techPrefix, true);
    }

    @Test
    @DisplayName("Should handle path with special characters")
    void getResourceWithSpecialCharacters() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "files/file-2024_v1.2.txt";
        String techPath = "user-" + userId + "/files/file-2024_v1.2.txt";
        String bucket = factory.manufacturePojo(String.class);

        Resource resource = factory.manufacturePojo(Resource.class);
        ResourceInfoResponseDTO expectedDto = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);
        when(s3Repo.getResourceByPath(bucket, techPath)).thenReturn(resource);
        when(resourceMapper.toDto(userId, resource)).thenReturn(expectedDto);

        ResourceInfoResponseDTO result = fileQueryService.getResource(userId, path);

        assertEquals(expectedDto, result);
        verify(s3Repo).getResourceByPath(bucket, techPath);
    }
}
