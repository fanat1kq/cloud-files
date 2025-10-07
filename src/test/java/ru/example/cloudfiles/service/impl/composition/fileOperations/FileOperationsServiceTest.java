package ru.example.cloudfiles.service.impl.composition.fileOperations;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.ResourceType;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class FileOperationsServiceTest {

    @Mock
    private FileQueryService fileQueryService;

    @Mock
    private FileDownloadService fileDownloadService;

    @Mock
    private FileMoveService fileMoveService;

    @Mock
    private FileDeleteService fileDeleteService;

    @InjectMocks
    private FileOperationsService fileOperationsService;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Get resource info successfully")
    void getResourceReturnsInfo(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class);
        ResourceInfoResponseDTO expectedResponse = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(fileQueryService.getResource(userId, path)).thenReturn(expectedResponse);

        ResourceInfoResponseDTO result = fileOperationsService.getResource(userId, path);

        softly.assertThat(result).isEqualTo(expectedResponse);
        verify(fileQueryService).getResource(userId, path);
    }

    @Test
    @DisplayName("Delete resource successfully")
    void deleteResourcePerformsDeletion() {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class);

        fileOperationsService.deleteResource(userId, path);

        verify(fileDeleteService).deleteResource(userId, path);
    }

    @Test
    @DisplayName("Prepare download successfully")
    void prepareDownloadReturnsDownloadResult(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class);
        DownloadResult expectedResult = factory.manufacturePojo(DownloadResult.class);

        when(fileDownloadService.prepareDownload(userId, path)).thenReturn(expectedResult);

        DownloadResult result = fileOperationsService.prepareDownload(userId, path);

        softly.assertThat(result).isEqualTo(expectedResult);
        verify(fileDownloadService).prepareDownload(userId, path);
    }

    @Test
    @DisplayName("Move resource successfully")
    void moveResourceReturnsMovedResourceInfo(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String oldPath = factory.manufacturePojo(String.class);
        String newPath = factory.manufacturePojo(String.class);
        ResourceInfoResponseDTO expectedResponse = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(fileMoveService.moveResource(userId, oldPath, newPath)).thenReturn(expectedResponse);

        ResourceInfoResponseDTO result = fileOperationsService.moveResource(userId, oldPath, newPath);

        softly.assertThat(result).isEqualTo(expectedResponse);
        verify(fileMoveService).moveResource(userId, oldPath, newPath);
    }

    @Test
    @DisplayName("Get resource with various data")
    void getResourceWithVariousDataReturnsInfo(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class);
        ResourceInfoResponseDTO expectedResponse = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(fileQueryService.getResource(userId, path)).thenReturn(expectedResponse);

        ResourceInfoResponseDTO result = fileOperationsService.getResource(userId, path);

        softly.assertThat(result).isEqualTo(expectedResponse);
        verify(fileQueryService).getResource(userId, path);
    }

    @Test
    @DisplayName("Delete resource with various data")
    void deleteResourceWithVariousDataPerformsDeletion() {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class);

        fileOperationsService.deleteResource(userId, path);

        verify(fileDeleteService).deleteResource(userId, path);
    }

    @Test
    @DisplayName("Prepare download with various data")
    void prepareDownloadWithVariousDataReturnsDownloadResult(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String path = factory.manufacturePojo(String.class);
        DownloadResult expectedResult = factory.manufacturePojo(DownloadResult.class);

        when(fileDownloadService.prepareDownload(userId, path)).thenReturn(expectedResult);

        DownloadResult result = fileOperationsService.prepareDownload(userId, path);

        softly.assertThat(result).isEqualTo(expectedResult);
        verify(fileDownloadService).prepareDownload(userId, path);
    }

    @Test
    @DisplayName("Move resource with various data")
    void moveResourceWithVariousDataReturnsMovedResourceInfo(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String oldPath = factory.manufacturePojo(String.class);
        String newPath = factory.manufacturePojo(String.class);
        ResourceInfoResponseDTO expectedResponse = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(fileMoveService.moveResource(userId, oldPath, newPath)).thenReturn(expectedResponse);

        ResourceInfoResponseDTO result = fileOperationsService.moveResource(userId, oldPath, newPath);

        softly.assertThat(result).isEqualTo(expectedResponse);
        verify(fileMoveService).moveResource(userId, oldPath, newPath);
    }

    @Test
    @DisplayName("Verify service interactions for get resource")
    void getResourceVerifiesServiceInteraction(SoftAssertions softly) {

        long userId = 123L;
        String path = "/documents/file.txt";
        ResourceInfoResponseDTO expectedResponse = ResourceInfoResponseDTO.builder()
                .path(path)
                .name("file.txt")
                .size(1024L)
                .type(ResourceType.FILE)
                .build();

        when(fileQueryService.getResource(userId, path)).thenReturn(expectedResponse);

        ResourceInfoResponseDTO result = fileOperationsService.getResource(userId, path);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.path()).isEqualTo(path);
        softly.assertThat(result.name()).isEqualTo("file.txt");
        softly.assertThat(result.type()).isEqualTo(ResourceType.FILE);
        verify(fileQueryService, times(1)).getResource(userId, path);
        verifyNoInteractions(fileDownloadService, fileMoveService, fileDeleteService);
    }

    @Test
    @DisplayName("Verify service interactions for delete resource")
    void deleteResourceVerifiesServiceInteraction() {

        long userId = 456L;
        String path = "/temp/to-delete";

        fileOperationsService.deleteResource(userId, path);

        verify(fileDeleteService, times(1)).deleteResource(userId, path);
        verifyNoInteractions(fileQueryService, fileDownloadService, fileMoveService);
    }

    @Test
    @DisplayName("Verify service interactions for prepare download")
    void prepareDownloadVerifiesServiceInteraction(SoftAssertions softly) {

        long userId = 789L;
        String path = "/downloads/file.pdf";
        StreamingResponseBody responseBody = outputStream -> {
        };
        DownloadResult expectedResult = new DownloadResult(responseBody, "attachment; filename=file.pdf");

        when(fileDownloadService.prepareDownload(userId, path)).thenReturn(expectedResult);

        DownloadResult result = fileOperationsService.prepareDownload(userId, path);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result).isEqualTo(expectedResult);
        verify(fileDownloadService, times(1)).prepareDownload(userId, path);
        verifyNoInteractions(fileQueryService, fileMoveService, fileDeleteService);
    }

    @Test
    @DisplayName("Verify service interactions for move resource")
    void moveResourceVerifiesServiceInteraction(SoftAssertions softly) {

        long userId = 999L;
        String oldPath = "/old/location";
        String newPath = "/new/location";
        ResourceInfoResponseDTO expectedResponse = ResourceInfoResponseDTO.builder()
                .path(newPath)
                .name("location")
                .size(0L)
                .type(ResourceType.DIRECTORY)
                .build();

        when(fileMoveService.moveResource(userId, oldPath, newPath)).thenReturn(expectedResponse);

        ResourceInfoResponseDTO result = fileOperationsService.moveResource(userId, oldPath, newPath);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.path()).isEqualTo(newPath);
        softly.assertThat(result.type()).isEqualTo(ResourceType.DIRECTORY);
        verify(fileMoveService, times(1)).moveResource(userId, oldPath, newPath);
        verifyNoInteractions(fileQueryService, fileDownloadService, fileDeleteService);
    }
}