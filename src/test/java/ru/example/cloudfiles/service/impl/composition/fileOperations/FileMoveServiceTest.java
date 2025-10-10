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
import ru.example.cloudfiles.exception.storageOperation.resource.ResourceAlreadyExistsException;
import ru.example.cloudfiles.exception.storageOperation.resource.ResourceNotFoundException;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class FileMoveServiceTest {

    @Mock
    private S3Repository s3Repo;

    @Mock
    private FileQueryService fileQueryService;

    @Mock
    private PathManager paths;

    @Mock
    private MinioProperties props;

    @InjectMocks
    private FileMoveService fileMoveService;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Should move file successfully")
    void moveFileOk() {

        long userId = factory.manufacturePojo(Long.class);
        String oldPath = "documents/file.txt";
        String newPath = "archive/file.txt";
        String oldTech = "user-" + userId + "/documents/file.txt";
        String newTech = "user-" + userId + "/archive/file.txt";

        Resource resource = new Resource(oldTech, new ByteArrayInputStream("content".getBytes()), 8L);
        ResourceInfoResponseDTO expectedDto = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(fileQueryService.resourceExists(userId, oldPath)).thenReturn(true);
        when(fileQueryService.resourceExists(userId, newPath)).thenReturn(false);
        when(paths.toTechnicalPath(userId, oldPath)).thenReturn(oldTech);
        when(paths.toTechnicalPath(userId, newPath)).thenReturn(newTech);
        when(paths.isDirectory(oldPath)).thenReturn(false);
        when(props.getBucket()).thenReturn("bucket");
        when(s3Repo.getResourceByPath("bucket", oldTech)).thenReturn(resource);
        when(fileQueryService.getResource(userId, newPath)).thenReturn(expectedDto);

        ResourceInfoResponseDTO result = fileMoveService.moveResource(userId, oldPath, newPath);

        assertEquals(expectedDto, result);
        verify(s3Repo).saveResource("bucket", newTech, resource.dataStream());
        verify(s3Repo).deleteResource("bucket", oldTech);
    }

    @Test
    @DisplayName("Should move directory successfully")
    void moveDirectoryOk() {

        long userId = factory.manufacturePojo(Long.class);
        String oldPath = "documents/";
        String newPath = "archive/";
        String oldTech = "user-" + userId + "/documents/";
        String newTech = "user-" + userId + "/archive/";

        List<String> sourcePaths = List.of(
                "user-" + userId + "/documents/file1.txt",
                "user-" + userId + "/documents/file2.txt"
        );

        Resource resource1 = new Resource(sourcePaths.get(0), new ByteArrayInputStream("content1".getBytes()), 9L);
        Resource resource2 = new Resource(sourcePaths.get(1), new ByteArrayInputStream("content2".getBytes()), 9L);
        ResourceInfoResponseDTO expectedDto = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(fileQueryService.resourceExists(userId, oldPath)).thenReturn(true);
        when(fileQueryService.resourceExists(userId, newPath)).thenReturn(false);
        when(paths.toTechnicalPath(userId, oldPath)).thenReturn(oldTech);
        when(paths.toTechnicalPath(userId, newPath)).thenReturn(newTech);
        when(paths.isDirectory(oldPath)).thenReturn(true);
        when(props.getBucket()).thenReturn("bucket");
        when(s3Repo.findAllNamesByPrefix("bucket", oldTech, true)).thenReturn(sourcePaths);
        when(s3Repo.getResourceByPath("bucket", sourcePaths.get(0))).thenReturn(resource1);
        when(s3Repo.getResourceByPath("bucket", sourcePaths.get(1))).thenReturn(resource2);
        when(fileQueryService.getResource(userId, newPath)).thenReturn(expectedDto);

        ResourceInfoResponseDTO result = fileMoveService.moveResource(userId, oldPath, newPath);

        assertEquals(expectedDto, result);

        verify(s3Repo, times(2)).saveResource(eq("bucket"), anyString(), any(InputStream.class));
        verify(s3Repo, times(3)).deleteResource(eq("bucket"), anyString());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when source does not exist")
    void moveResourceNotFound() {

        long userId = factory.manufacturePojo(Long.class);
        String oldPath = "nonexistent/file.txt";
        String newPath = "documents/file.txt";

        when(fileQueryService.resourceExists(userId, oldPath)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> fileMoveService.moveResource(userId, oldPath, newPath));

        verify(s3Repo, never()).saveResource(any(), any(), any());
        verify(s3Repo, never()).deleteResource(any(), any());
    }

    @Test
    @DisplayName("Should throw ResourceAlreadyExistsException when destination exists")
    void moveResourceAlreadyExists() {

        long userId = factory.manufacturePojo(Long.class);
        String oldPath = "documents/file.txt";
        String newPath = "existing/file.txt";

        when(fileQueryService.resourceExists(userId, oldPath)).thenReturn(true);
        when(fileQueryService.resourceExists(userId, newPath)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> fileMoveService.moveResource(userId, oldPath, newPath));

        verify(s3Repo, never()).saveResource(any(), any(), any());
        verify(s3Repo, never()).deleteResource(any(), any());
    }

    @Test
    @DisplayName("Should handle directory without trailing slash")
    void moveDirectoryWithoutTrailingSlash() {

        long userId = factory.manufacturePojo(Long.class);
        String oldPath = "documents";
        String newPath = "archive";
        String oldTech = "user-" + userId + "/documents";
        String newTech = "user-" + userId + "/archive";

        List<String> sourcePaths = List.of(
                "user-" + userId + "/documents/file1.txt"
        );

        Resource resource = new Resource(sourcePaths.getFirst(), new ByteArrayInputStream("content".getBytes()), 8L);
        ResourceInfoResponseDTO expectedDto = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(fileQueryService.resourceExists(userId, oldPath)).thenReturn(true);
        when(fileQueryService.resourceExists(userId, newPath)).thenReturn(false);
        when(paths.toTechnicalPath(userId, oldPath)).thenReturn(oldTech);
        when(paths.toTechnicalPath(userId, newPath)).thenReturn(newTech);
        when(paths.isDirectory(oldPath)).thenReturn(true);
        when(props.getBucket()).thenReturn("bucket");
        when(s3Repo.findAllNamesByPrefix("bucket", oldTech + "/", true)).thenReturn(sourcePaths);
        when(s3Repo.getResourceByPath("bucket", sourcePaths.getFirst())).thenReturn(resource);
        when(fileQueryService.getResource(userId, newPath)).thenReturn(expectedDto);

        ResourceInfoResponseDTO result = fileMoveService.moveResource(userId, oldPath, newPath);

        assertEquals(expectedDto, result);
        verify(s3Repo).saveResource(eq("bucket"), anyString(), any(InputStream.class));
        verify(s3Repo).deleteResource("bucket", sourcePaths.getFirst());
    }

    @Test
    @DisplayName("Should move empty directory")
    void moveEmptyDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String oldPath = "empty/";
        String newPath = "archive/";
        String oldTech = "user-" + userId + "/empty/";
        String newTech = "user-" + userId + "/archive/";

        List<String> sourcePaths = List.of();

        ResourceInfoResponseDTO expectedDto = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(fileQueryService.resourceExists(userId, oldPath)).thenReturn(true);
        when(fileQueryService.resourceExists(userId, newPath)).thenReturn(false);
        when(paths.toTechnicalPath(userId, oldPath)).thenReturn(oldTech);
        when(paths.toTechnicalPath(userId, newPath)).thenReturn(newTech);
        when(paths.isDirectory(oldPath)).thenReturn(true);
        when(props.getBucket()).thenReturn("bucket");
        when(s3Repo.findAllNamesByPrefix("bucket", oldTech, true)).thenReturn(sourcePaths);
        when(fileQueryService.getResource(userId, newPath)).thenReturn(expectedDto);

        ResourceInfoResponseDTO result = fileMoveService.moveResource(userId, oldPath, newPath);

        assertEquals(expectedDto, result);

        verify(s3Repo, never()).saveResource(any(), any(), any());
        verify(s3Repo).deleteResource("bucket", oldTech);
    }
}