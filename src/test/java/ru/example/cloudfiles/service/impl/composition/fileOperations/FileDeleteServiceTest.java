package ru.example.cloudfiles.service.impl.composition.fileOperations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class FileDeleteServiceTest {

    @Mock
    private S3Repository s3Repo;

    @Mock
    private FileQueryService fileQueryService;

    @Mock
    private PathManager paths;

    @Mock
    private MinioProperties props;

    @InjectMocks
    private FileDeleteService fileDeleteService;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Should delete resource successfully")
    void deleteResourceOk() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/file.txt";
        String bucket = factory.manufacturePojo(String.class);
        String techPath = "user-" + userId + "/documents/file.txt";

        when(fileQueryService.resourceExists(userId, path)).thenReturn(true);
        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);

        assertDoesNotThrow(() -> fileDeleteService.deleteResource(userId, path));

        verify(fileQueryService).resourceExists(userId, path);
        verify(s3Repo).deleteResource(bucket, techPath);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when resource does not exist")
    void deleteResourceNotFound() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "nonexistent/file.txt";

        when(fileQueryService.resourceExists(userId, path)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> fileDeleteService.deleteResource(userId, path));

        verify(fileQueryService).resourceExists(userId, path);
        verify(s3Repo, never()).deleteResource(any(), any());
    }

    @Test
    @DisplayName("Should handle directory path")
    void deleteDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/";
        String bucket = factory.manufacturePojo(String.class);
        String techPath = "user-" + userId + "/documents/";

        when(fileQueryService.resourceExists(userId, path)).thenReturn(true);
        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);

        assertDoesNotThrow(() -> fileDeleteService.deleteResource(userId, path));

        verify(fileQueryService).resourceExists(userId, path);
        verify(s3Repo).deleteResource(bucket, techPath);
    }

    @Test
    @DisplayName("Should handle root file path")
    void deleteRootFile() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "file.txt";
        String bucket = factory.manufacturePojo(String.class);
        String techPath = "user-" + userId + "/file.txt";

        when(fileQueryService.resourceExists(userId, path)).thenReturn(true);
        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);

        assertDoesNotThrow(() -> fileDeleteService.deleteResource(userId, path));

        verify(fileQueryService).resourceExists(userId, path);
        verify(s3Repo).deleteResource(bucket, techPath);
    }

    @Test
    @DisplayName("Should handle nested directory path")
    void deleteNestedDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "projects/src/main/java/";
        String bucket = factory.manufacturePojo(String.class);
        String techPath = "user-" + userId + "/projects/src/main/java/";

        when(fileQueryService.resourceExists(userId, path)).thenReturn(true);
        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);

        assertDoesNotThrow(() -> fileDeleteService.deleteResource(userId, path));

        verify(fileQueryService).resourceExists(userId, path);
        verify(s3Repo).deleteResource(bucket, techPath);
    }

    @Test
    @DisplayName("Should handle path with special characters")
    void deleteResourceWithSpecialCharacters() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "files/file-2024_v1.2.txt";
        String bucket = factory.manufacturePojo(String.class);
        String techPath = "user-" + userId + "/files/file-2024_v1.2.txt";

        when(fileQueryService.resourceExists(userId, path)).thenReturn(true);
        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(techPath);

        assertDoesNotThrow(() -> fileDeleteService.deleteResource(userId, path));

        verify(fileQueryService).resourceExists(userId, path);
        verify(s3Repo).deleteResource(bucket, techPath);
    }

    @Test
    @DisplayName("Should verify correct technical path conversion")
    void verifyTechnicalPathConversion() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/report.pdf";
        String bucket = factory.manufacturePojo(String.class);
        String expectedTechPath = "user-" + userId + "/documents/report.pdf";

        when(fileQueryService.resourceExists(userId, path)).thenReturn(true);
        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, path)).thenReturn(expectedTechPath);

        fileDeleteService.deleteResource(userId, path);

        verify(paths).toTechnicalPath(userId, path);
        verify(s3Repo).deleteResource(bucket, expectedTechPath);
    }

    @Test
    @DisplayName("Should not delete when resource check fails")
    void deleteResourceCheckFails() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "important/file.txt";

        when(fileQueryService.resourceExists(userId, path)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> fileDeleteService.deleteResource(userId, path));

        verify(fileQueryService).resourceExists(userId, path);
        verify(s3Repo, never()).deleteResource(any(), any());
        verify(paths, never()).toTechnicalPath(anyLong(), anyString());
    }
}