package ru.example.cloudfiles.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.cloudfiles.config.properties.MinioProperties;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PathManagerTest {

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private PathManager pathManager;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Should return user directory with correct pattern")
    void getUserDirectoryOk() {

        long userId = factory.manufacturePojo(Long.class);
        String expectedPattern = "user-%d/";

        when(minioProperties.getUserDirectoryPattern()).thenReturn(expectedPattern);

        String result = pathManager.getUserDirectory(userId);

        assertEquals(expectedPattern.formatted(userId), result);
        verify(minioProperties).getUserDirectoryPattern();
    }

    @Test
    @DisplayName("Should convert user path to technical path")
    void toTechnicalPathOk() {

        long userId = factory.manufacturePojo(Long.class);
        String userPath = factory.manufacturePojo(String.class);
        String userDirectory = "user-" + userId + "/";

        when(minioProperties.getUserDirectoryPattern()).thenReturn("user-%d/");

        String result = pathManager.toTechnicalPath(userId, userPath);

        assertEquals(userDirectory + userPath, result);
    }

    @Test
    @DisplayName("Should convert technical path to user path when starts with user directory")
    void toUserPathWithUserDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String userDirectory = "user-" + userId + "/";
        String technicalPath = userDirectory + "documents/file.txt";

        when(minioProperties.getUserDirectoryPattern()).thenReturn("user-%d/");

        String result = pathManager.toUserPath(userId, technicalPath);

        assertEquals("documents/file.txt", result);
    }

    @Test
    @DisplayName("Should return original path when technical path does not start with user directory")
    void toUserPathWithoutUserDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String technicalPath = "other-user/documents/file.txt";

        when(minioProperties.getUserDirectoryPattern()).thenReturn("user-%d/");

        String result = pathManager.toUserPath(userId, technicalPath);

        assertEquals(technicalPath, result);
    }

    @Test
    @DisplayName("Should return true for directory path")
    void isDirectoryWithDirectoryPath() {

        String directoryPath = factory.manufacturePojo(String.class) + "/";

        boolean result = pathManager.isDirectory(directoryPath);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false for file path")
    void isDirectoryWithFilePath() {

        String filePath = factory.manufacturePojo(String.class) + ".txt";

        boolean result = pathManager.isDirectory(filePath);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false for single slash")
    void isDirectoryWithSingleSlash() {

        boolean result = pathManager.isDirectory("/");

        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle complex user directory patterns")
    void getUserDirectoryComplexPattern() {

        long userId = factory.manufacturePojo(Long.class);
        String complexPattern = "users/%d/files/";

        when(minioProperties.getUserDirectoryPattern()).thenReturn(complexPattern);

        String result = pathManager.getUserDirectory(userId);

        assertEquals(complexPattern.formatted(userId), result);
    }

    @Test
    @DisplayName("Should handle empty user path in technical path conversion")
    void toTechnicalPathEmptyUserPath() {

        long userId = factory.manufacturePojo(Long.class);
        String userDirectory = "user-" + userId + "/";

        when(minioProperties.getUserDirectoryPattern()).thenReturn("user-%d/");

        String result = pathManager.toTechnicalPath(userId, "");

        assertEquals(userDirectory, result);
    }

    @Test
    @DisplayName("Should handle technical path exactly matching user directory")
    void toUserPathExactUserDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String userDirectory = "user-" + userId + "/";

        when(minioProperties.getUserDirectoryPattern()).thenReturn("user-%d/");

        String result = pathManager.toUserPath(userId, userDirectory);

        assertEquals("", result);
    }
}
