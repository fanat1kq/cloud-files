package ru.example.cloudfiles.mapper;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.dto.ResourceType;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.service.impl.PathManager;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class ResourceMapperTest {

    @Mock
    private PathManager paths;

    @InjectMocks
    private ResourceMapper resourceMapper;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Should map file Resource to ResourceInfoResponseDTO")
    void toDtoFile(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String techPath = "user-" + userId + "/documents/file.txt";
        String userPath = "documents/file.txt";
        Resource resource = new Resource(techPath, mock(InputStream.class), 1024L);

        when(paths.isDirectory(techPath)).thenReturn(false);
        when(paths.toUserPath(userId, techPath)).thenReturn(userPath);
        when(paths.getUserDirectory(userId)).thenReturn("user-" + userId + "/");

        ResourceInfoResponseDTO result = resourceMapper.toDto(userId, resource);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.path()).isEqualTo("documents/");
        softly.assertThat(result.name()).isEqualTo("file.txt");
        softly.assertThat(result.size()).isEqualTo(1024L);
        softly.assertThat(result.type()).isEqualTo(ResourceType.FILE);
    }

    @Test
    @DisplayName("Should map directory Resource to ResourceInfoResponseDTO")
    void toDtoDirectory(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String techPath = "user-" + userId + "/documents/";
        String userPath = "documents/";
        Resource resource = new Resource(techPath, mock(InputStream.class), 0L);

        when(paths.isDirectory(techPath)).thenReturn(true);
        when(paths.toUserPath(userId, techPath)).thenReturn(userPath);
        when(paths.getUserDirectory(userId)).thenReturn("user-" + userId + "/");

        ResourceInfoResponseDTO result = resourceMapper.toDto(userId, resource);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.path()).isEqualTo("");
        softly.assertThat(result.name()).isEqualTo("documents/");
        softly.assertThat(result.size()).isEqualTo(0L);
        softly.assertThat(result.type()).isEqualTo(ResourceType.DIRECTORY);
    }

    @Test
    @DisplayName("Should map nested directory Resource to ResourceInfoResponseDTO")
    void toDtoNestedDirectory(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String techPath = "user-" + userId + "/projects/src/";
        String userPath = "projects/src/";
        Resource resource = new Resource(techPath, mock(InputStream.class), 0L);

        when(paths.isDirectory(techPath)).thenReturn(true);
        when(paths.toUserPath(userId, techPath)).thenReturn(userPath);
        when(paths.getUserDirectory(userId)).thenReturn("user-" + userId + "/");

        ResourceInfoResponseDTO result = resourceMapper.toDto(userId, resource);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.path()).isEqualTo("projects/");
        softly.assertThat(result.name()).isEqualTo("src/");
        softly.assertThat(result.size()).isEqualTo(0L);
        softly.assertThat(result.type()).isEqualTo(ResourceType.DIRECTORY);
    }

    @Test
    @DisplayName("Should map root file Resource to ResourceInfoResponseDTO")
    void toDtoRootFile(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String techPath = "user-" + userId + "/file.txt";
        String userPath = "file.txt";
        Resource resource = new Resource(techPath, mock(InputStream.class), 512L);

        when(paths.isDirectory(techPath)).thenReturn(false);
        when(paths.toUserPath(userId, techPath)).thenReturn(userPath);
        when(paths.getUserDirectory(userId)).thenReturn("user-" + userId + "/");

        ResourceInfoResponseDTO result = resourceMapper.toDto(userId, resource);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.path()).isEqualTo("");
        softly.assertThat(result.name()).isEqualTo("file.txt");
        softly.assertThat(result.size()).isEqualTo(512L);
        softly.assertThat(result.type()).isEqualTo(ResourceType.FILE);
    }

    @Test
    @DisplayName("Should handle complex user directory pattern")
    void toDtoComplexUserDirectory(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String techPath = "users/" + userId + "/files/documents/report.pdf";
        String userPath = "documents/report.pdf";
        Resource resource = new Resource(techPath, mock(InputStream.class), 3072L);

        when(paths.isDirectory(techPath)).thenReturn(false);
        when(paths.toUserPath(userId, techPath)).thenReturn(userPath);
        when(paths.getUserDirectory(userId)).thenReturn("users/" + userId + "/files/");

        ResourceInfoResponseDTO result = resourceMapper.toDto(userId, resource);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.path()).isEqualTo("documents/");
        softly.assertThat(result.name()).isEqualTo("report.pdf");
        softly.assertThat(result.size()).isEqualTo(3072L);
        softly.assertThat(result.type()).isEqualTo(ResourceType.FILE);
    }

    @Test
    @DisplayName("Should handle file with dots in name")
    void toDtoFileWithDots(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String techPath = "user-" + userId + "/files/config.backup.json";
        String userPath = "files/config.backup.json";
        Resource resource = new Resource(techPath, mock(InputStream.class), 128L);

        when(paths.isDirectory(techPath)).thenReturn(false);
        when(paths.toUserPath(userId, techPath)).thenReturn(userPath);
        when(paths.getUserDirectory(userId)).thenReturn("user-" + userId + "/");

        ResourceInfoResponseDTO result = resourceMapper.toDto(userId, resource);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.path()).isEqualTo("files/");
        softly.assertThat(result.name()).isEqualTo("config.backup.json");
        softly.assertThat(result.size()).isEqualTo(128L);
        softly.assertThat(result.type()).isEqualTo(ResourceType.FILE);
    }

    @Test
    @DisplayName("Should handle empty directory")
    void toDtoEmptyDirectory(SoftAssertions softly) {

        long userId = factory.manufacturePojo(Long.class);
        String techPath = "user-" + userId + "/empty/";
        String userPath = "empty/";
        Resource resource = new Resource(techPath, mock(InputStream.class), 0L);

        when(paths.isDirectory(techPath)).thenReturn(true);
        when(paths.toUserPath(userId, techPath)).thenReturn(userPath);
        when(paths.getUserDirectory(userId)).thenReturn("user-" + userId + "/");

        ResourceInfoResponseDTO result = resourceMapper.toDto(userId, resource);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.path()).isEqualTo("");
        softly.assertThat(result.name()).isEqualTo("empty/");
        softly.assertThat(result.size()).isEqualTo(0L);
        softly.assertThat(result.type()).isEqualTo(ResourceType.DIRECTORY);
    }
}