package ru.example.cloudfiles.service.impl.composition.fileOperations;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.exception.storageOperationImpl.directory.ZipCreationException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDownloadServiceTest {

    @Mock
    private S3Repository s3Repo;

    @Mock
    private FileQueryService fileQueryService;

    @Mock
    private PathManager paths;

    @Mock
    private MinioProperties props;

    @InjectMocks
    private FileDownloadService fileDownloadService;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Should prepare download for single file")
    void prepareDownloadSingleFile() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/file.txt";
        String fileName = "file.txt";
        String resourceName = "user-" + userId + "/documents/file.txt";

        when(fileQueryService.findAllNames(userId, path)).thenReturn(List.of(resourceName));

        DownloadResult result = fileDownloadService.prepareDownload(userId, path);

        assertNotNull(result);
        assertNotNull(result.streamingBody());
        assertTrue(result.contentDisposition().contains(fileName));
    }

    @Test
    @DisplayName("Should prepare download for directory")
    void prepareDownloadDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/";
        String fileName = "documents.zip";

        when(fileQueryService.findAllNames(userId, path)).thenReturn(List.of(
                "user-" + userId + "/documents/file1.txt",
                "user-" + userId + "/documents/file2.txt"
        ));

        DownloadResult result = fileDownloadService.prepareDownload(userId, path);

        assertNotNull(result);
        assertNotNull(result.streamingBody());
        assertTrue(result.contentDisposition().contains(fileName));
    }

    @Test
    @SneakyThrows
    @DisplayName("Should create single file response")
    void createSingleFileResponse() {

        long userId = 123L;
        String path = "documents/file.txt";
        String resourceName = "user-" + userId + "/documents/file.txt";

        InputStream inputStream = new ByteArrayInputStream("file content".getBytes());
        Resource resource = new Resource(resourceName, inputStream, 12L);

        when(fileQueryService.findAllNames(userId, path)).thenReturn(List.of(resourceName));
        when(s3Repo.getResourceByPath(any(), eq(resourceName))).thenReturn(resource);
        when(props.getBucket()).thenReturn("bucket");

        StreamingResponseBody responseBody = fileDownloadService.download(userId, path);

        assertNotNull(responseBody);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        responseBody.writeTo(outputStream);

        assertEquals("file content", outputStream.toString());
    }

    @Test
    @DisplayName("Should extract file name from path")
    void extractFileNameFromPath() {

        assertDoesNotThrow(() -> {
            long userId = factory.manufacturePojo(Long.class);
            String path = "documents/file.txt";
            String resourceName = "user-" + userId + "/documents/file.txt";

            when(fileQueryService.findAllNames(userId, path)).thenReturn(List.of(resourceName));

            DownloadResult result = fileDownloadService.prepareDownload(userId, path);

            assertTrue(result.contentDisposition().contains("file.txt"));
        });
    }

    @Test
    @DisplayName("Should add file to zip")
    void addFileToZip() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/";
        List<String> resourceNames = List.of(
                "user-" + userId + "/documents/file.txt"
        );

        InputStream inputStream = new ByteArrayInputStream("file content".getBytes());
        Resource resource = new Resource("user-" + userId + "/documents/file.txt", inputStream, 12L);

        when(fileQueryService.findAllNames(userId, path)).thenReturn(resourceNames);
        when(s3Repo.getResourceByPath(any(), any())).thenReturn(resource);

        lenient().when(paths.toUserPath(userId, "user-" + userId + "/documents/file.txt")).thenReturn("documents/file.txt");
        lenient().when(paths.isDirectory("user-" + userId + "/documents/file.txt")).thenReturn(false);

        when(props.getBucket()).thenReturn("bucket");

        StreamingResponseBody responseBody = fileDownloadService.download(userId, path);

        assertNotNull(responseBody);
        assertDoesNotThrow(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            responseBody.writeTo(outputStream);
            assertTrue(outputStream.toByteArray().length > 0);
        });
    }

    @Test
    @DisplayName("Should add directory to zip without content")
    void addDirectoryToZip() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/";
        List<String> resourceNames = List.of(
                "user-" + userId + "/documents/file1.txt",
                "user-" + userId + "/documents/file2.txt"
        );

        InputStream stream1 = new ByteArrayInputStream("content1".getBytes());
        InputStream stream2 = new ByteArrayInputStream("content2".getBytes());
        Resource resource1 = new Resource("user-" + userId + "/documents/file1.txt", stream1, 8L);
        Resource resource2 = new Resource("user-" + userId + "/documents/file2.txt", stream2, 8L);

        when(fileQueryService.findAllNames(userId, path)).thenReturn(resourceNames);
        when(s3Repo.getResourceByPath(any(), eq("user-" + userId + "/documents/file1.txt"))).thenReturn(resource1);
        when(s3Repo.getResourceByPath(any(), eq("user-" + userId + "/documents/file2.txt"))).thenReturn(resource2);
        when(paths.toUserPath(anyLong(), anyString())).thenAnswer(invocation -> {
            String techPath = invocation.getArgument(1);
            return techPath.substring(("user-" + userId + "/").length());
        });
        when(paths.isDirectory(anyString())).thenReturn(false);
        when(props.getBucket()).thenReturn("bucket");

        StreamingResponseBody responseBody = fileDownloadService.download(userId, path);

        assertNotNull(responseBody);
        assertDoesNotThrow(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            responseBody.writeTo(outputStream);
            assertTrue(outputStream.toByteArray().length > 0);
        });
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when single file download fails")
    void singleFileDownloadFails() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/file.txt";
        List<String> resourceNames = List.of(
                "user-" + userId + "/documents/file.txt"
        );

        InputStream brokenStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Stream error");
            }
        };
        Resource resource = new Resource("user-" + userId + "/documents/file.txt", brokenStream, 12L);

        when(fileQueryService.findAllNames(userId, path)).thenReturn(resourceNames);
        when(s3Repo.getResourceByPath(any(), any())).thenReturn(resource);
        when(props.getBucket()).thenReturn("bucket");

        StreamingResponseBody responseBody = fileDownloadService.download(userId, path);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(ResourceNotFoundException.class, () -> responseBody.writeTo(outputStream));
    }

    @Test
    @DisplayName("Should throw ZipCreationException when zip creation fails")
    void zipCreationFails() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "documents/";
        List<String> resourceNames = List.of(
                "user-" + userId + "/documents/file1.txt",
                "user-" + userId + "/documents/file2.txt"
        );

        InputStream brokenStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Stream error");
            }
        };
        Resource brokenResource = new Resource("user-" + userId + "/documents/file1.txt", brokenStream, 12L);

        when(fileQueryService.findAllNames(userId, path)).thenReturn(resourceNames);
        when(s3Repo.getResourceByPath(any(), any())).thenReturn(brokenResource);
        when(paths.toUserPath(anyLong(), anyString())).thenReturn("documents/file.txt");
        when(paths.isDirectory(anyString())).thenReturn(false);
        when(props.getBucket()).thenReturn("bucket");

        StreamingResponseBody responseBody = fileDownloadService.download(userId, path);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(ZipCreationException.class, () -> responseBody.writeTo(outputStream));
    }

    @Test
    @DisplayName("Should handle URL encoding in content disposition")
    void handleUrlEncodingInContentDisposition() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "files/документ.txt";
        String resourceName = "user-" + userId + "/files/документ.txt";

        when(fileQueryService.findAllNames(userId, path)).thenReturn(List.of(resourceName));

        DownloadResult result = fileDownloadService.prepareDownload(userId, path);

        assertNotNull(result);
        assertTrue(result.contentDisposition().contains("UTF-8"));
        assertTrue(result.contentDisposition().contains("attachment"));
    }

    @Test
    @DisplayName("Should handle empty file list")
    void handleEmptyFileList() {

        long userId = factory.manufacturePojo(Long.class);
        String path = "empty/";

        when(fileQueryService.findAllNames(userId, path)).thenReturn(List.of());

        StreamingResponseBody responseBody = fileDownloadService.download(userId, path);

        assertNotNull(responseBody);
        assertDoesNotThrow(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            responseBody.writeTo(outputStream);
        });
    }
}