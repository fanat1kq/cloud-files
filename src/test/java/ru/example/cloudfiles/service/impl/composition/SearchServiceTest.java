package ru.example.cloudfiles.service.impl.composition;

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
import ru.example.cloudfiles.exception.validation.InvalidSearchQueryException;
import ru.example.cloudfiles.mapper.ResourceMapper;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private S3Repository s3Repo;

    @Mock
    private PathManager paths;

    @Mock
    private MinioProperties props;

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private SearchService searchService;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Should search resources successfully with matching query")
    void searchOk() {

        long userId = factory.manufacturePojo(Long.class);
        String query = "doc";
        String bucket = factory.manufacturePojo(String.class);
        String userDirectory = "user-" + userId + "/";

        List<String> technicalPaths = List.of(
                userDirectory + "document.txt",
                userDirectory + "image.jpg",
                userDirectory + "documents/file.pdf",
                userDirectory + "photos/picture.png",
                userDirectory + "readme.doc"
        );

        List<String> filteredUserPaths = List.of(
                "document.txt",
                "documents/file.pdf",
                "readme.doc"
        );

        Resource resource1 = factory.manufacturePojo(Resource.class);
        Resource resource2 = factory.manufacturePojo(Resource.class);
        Resource resource3 = factory.manufacturePojo(Resource.class);
        List<Resource> resources = List.of(resource1, resource2, resource3);

        ResourceInfoResponseDTO dto1 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        ResourceInfoResponseDTO dto2 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        ResourceInfoResponseDTO dto3 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        List<ResourceInfoResponseDTO> expectedDtos = List.of(dto1, dto2, dto3);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, "")).thenReturn(userDirectory);
        when(s3Repo.findAllNamesByPrefix(bucket, userDirectory, true)).thenReturn(technicalPaths);

        for (String technicalPath : technicalPaths) {
            when(paths.toUserPath(userId, technicalPath)).thenReturn(technicalPath.substring(userDirectory.length()));
        }

        for (int i = 0; i < filteredUserPaths.size(); i++) {
            String userPath = filteredUserPaths.get(i);
            String technicalPath = userDirectory + userPath;
            when(paths.toTechnicalPath(userId, userPath)).thenReturn(technicalPath);
            when(s3Repo.getResourceByPath(bucket, technicalPath)).thenReturn(resources.get(i));
            when(resourceMapper.toDto(userId, resources.get(i))).thenReturn(expectedDtos.get(i));
        }

        List<ResourceInfoResponseDTO> result = searchService.search(userId, query);

        assertEquals(filteredUserPaths.size(), result.size());
        verify(s3Repo).findAllNamesByPrefix(bucket, userDirectory, true);
        verify(s3Repo, times(filteredUserPaths.size())).getResourceByPath(eq(bucket), anyString());
        verify(resourceMapper, times(filteredUserPaths.size())).toDto(eq(userId), any(Resource.class));
    }

    @Test
    @DisplayName("Should filter resources by query")
    void searchWithFiltering() {

        long userId = factory.manufacturePojo(Long.class);
        String query = "doc";
        String bucket = factory.manufacturePojo(String.class);
        String userDirectory = factory.manufacturePojo(String.class);

        List<String> technicalPaths = List.of(
                userDirectory + "document.txt",
                userDirectory + "image.jpg",
                userDirectory + "documents/file.pdf",
                userDirectory + "readme.txt"
        );

        List<String> matchingUserPaths = List.of("document.txt", "documents/file.pdf");

        Resource resource1 = factory.manufacturePojo(Resource.class);
        Resource resource2 = factory.manufacturePojo(Resource.class);
        List<Resource> matchingResources = List.of(resource1, resource2);

        ResourceInfoResponseDTO dto1 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        ResourceInfoResponseDTO dto2 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        List<ResourceInfoResponseDTO> expectedDtos = List.of(dto1, dto2);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, "")).thenReturn(userDirectory);
        when(s3Repo.findAllNamesByPrefix(bucket, userDirectory, true)).thenReturn(technicalPaths);

        for (String techPath : technicalPaths) {
            String userPath = techPath.substring(userDirectory.length());
            when(paths.toUserPath(userId, techPath)).thenReturn(userPath);
        }

        for (int i = 0; i < matchingUserPaths.size(); i++) {
            String userPath = matchingUserPaths.get(i);
            String techPath = userDirectory + userPath;
            when(paths.toTechnicalPath(userId, userPath)).thenReturn(techPath);
            when(s3Repo.getResourceByPath(bucket, techPath)).thenReturn(matchingResources.get(i));
            when(resourceMapper.toDto(userId, matchingResources.get(i))).thenReturn(expectedDtos.get(i));
        }

        List<ResourceInfoResponseDTO> result = searchService.search(userId, query);

        assertEquals(matchingUserPaths.size(), result.size());
        verify(s3Repo, times(matchingUserPaths.size())).getResourceByPath(eq(bucket), anyString());
        verify(resourceMapper, times(matchingUserPaths.size())).toDto(eq(userId), any(Resource.class));
    }

    @Test
    @DisplayName("Should handle case insensitive search")
    void searchCaseInsensitive() {

        long userId = factory.manufacturePojo(Long.class);
        String query = "DOC";
        String bucket = factory.manufacturePojo(String.class);
        String userDirectory = factory.manufacturePojo(String.class);

        List<String> technicalPaths = List.of(
                userDirectory + "document.txt",
                userDirectory + "DOCUMENT.pdf"
        );

        Resource resource1 = factory.manufacturePojo(Resource.class);
        Resource resource2 = factory.manufacturePojo(Resource.class);
        List<Resource> resources = List.of(resource1, resource2);

        ResourceInfoResponseDTO dto1 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        ResourceInfoResponseDTO dto2 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        List<ResourceInfoResponseDTO> expectedDtos = List.of(dto1, dto2);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, "")).thenReturn(userDirectory);
        when(s3Repo.findAllNamesByPrefix(bucket, userDirectory, true)).thenReturn(technicalPaths);

        for (int i = 0; i < technicalPaths.size(); i++) {
            String userPath = technicalPaths.get(i).substring(userDirectory.length());
            when(paths.toUserPath(userId, technicalPaths.get(i))).thenReturn(userPath);
            when(paths.toTechnicalPath(userId, userPath)).thenReturn(technicalPaths.get(i));
            when(s3Repo.getResourceByPath(bucket, technicalPaths.get(i))).thenReturn(resources.get(i));
            when(resourceMapper.toDto(userId, resources.get(i))).thenReturn(expectedDtos.get(i));
        }

        List<ResourceInfoResponseDTO> result = searchService.search(userId, query);

        assertEquals(technicalPaths.size(), result.size());
        verify(s3Repo, times(technicalPaths.size())).getResourceByPath(eq(bucket), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidSearchQueryException when query is null")
    void searchNullQuery() {

        long userId = factory.manufacturePojo(Long.class);

        assertThrows(InvalidSearchQueryException.class, () -> searchService.search(userId, null));
        verify(s3Repo, never()).findAllNamesByPrefix(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw InvalidSearchQueryException when query is empty")
    void searchEmptyQuery() {

        long userId = factory.manufacturePojo(Long.class);

        assertThrows(InvalidSearchQueryException.class, () -> searchService.search(userId, ""));
        verify(s3Repo, never()).findAllNamesByPrefix(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw InvalidSearchQueryException when query is only whitespace")
    void searchWhitespaceQuery() {

        long userId = factory.manufacturePojo(Long.class);

        assertThrows(InvalidSearchQueryException.class, () -> searchService.search(userId, "   "));
        verify(s3Repo, never()).findAllNamesByPrefix(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Should return empty list when no resources match query")
    void searchNoMatches() {

        long userId = factory.manufacturePojo(Long.class);
        String query = "nonexistent";
        String bucket = factory.manufacturePojo(String.class);
        String userDirectory = factory.manufacturePojo(String.class);

        List<String> technicalPaths = List.of(
                userDirectory + "document.txt",
                userDirectory + "image.jpg"
        );

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, "")).thenReturn(userDirectory);
        when(s3Repo.findAllNamesByPrefix(bucket, userDirectory, true)).thenReturn(technicalPaths);

        for (String techPath : technicalPaths) {
            String userPath = techPath.substring(userDirectory.length());
            when(paths.toUserPath(userId, techPath)).thenReturn(userPath);
        }

        List<ResourceInfoResponseDTO> result = searchService.search(userId, query);

        assertTrue(result.isEmpty());
        verify(s3Repo, never()).getResourceByPath(any(), any());
        verify(resourceMapper, never()).toDto(anyLong(), any(Resource.class));
    }

    @Test
    @DisplayName("Should handle query with special characters")
    void searchSpecialCharacters() {

        long userId = factory.manufacturePojo(Long.class);
        String query = "file-2024_v1.2";
        String bucket = factory.manufacturePojo(String.class);
        String userDirectory = factory.manufacturePojo(String.class);

        List<String> technicalPaths = List.of(
                userDirectory + "file-2024_v1.2.txt"
        );

        Resource resource = factory.manufacturePojo(Resource.class);
        ResourceInfoResponseDTO expectedDto = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, "")).thenReturn(userDirectory);
        when(s3Repo.findAllNamesByPrefix(bucket, userDirectory, true)).thenReturn(technicalPaths);

        String userPath = technicalPaths.getFirst().substring(userDirectory.length());
        when(paths.toUserPath(userId, technicalPaths.getFirst())).thenReturn(userPath);
        when(paths.toTechnicalPath(userId, userPath)).thenReturn(technicalPaths.getFirst());
        when(s3Repo.getResourceByPath(bucket, technicalPaths.getFirst())).thenReturn(resource);
        when(resourceMapper.toDto(userId, resource)).thenReturn(expectedDto);

        List<ResourceInfoResponseDTO> result = searchService.search(userId, query);

        assertEquals(1, result.size());
        assertEquals(expectedDto, result.getFirst());
        verify(s3Repo).getResourceByPath(bucket, technicalPaths.getFirst());
    }

    @Test
    @DisplayName("Should normalize query by trimming and converting to lowercase")
    void searchNormalizesQuery() {

        long userId = factory.manufacturePojo(Long.class);
        String rawQuery = "  DOCument  ";
        String bucket = factory.manufacturePojo(String.class);
        String userDirectory = factory.manufacturePojo(String.class);

        List<String> technicalPaths = List.of(
                userDirectory + "document.txt"
        );

        Resource resource = factory.manufacturePojo(Resource.class);
        ResourceInfoResponseDTO expectedDto = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.toTechnicalPath(userId, "")).thenReturn(userDirectory);
        when(s3Repo.findAllNamesByPrefix(bucket, userDirectory, true)).thenReturn(technicalPaths);

        String userPath = technicalPaths.getFirst().substring(userDirectory.length());
        when(paths.toUserPath(userId, technicalPaths.getFirst())).thenReturn(userPath);
        when(paths.toTechnicalPath(userId, userPath)).thenReturn(technicalPaths.getFirst());
        when(s3Repo.getResourceByPath(bucket, technicalPaths.getFirst())).thenReturn(resource);
        when(resourceMapper.toDto(userId, resource)).thenReturn(expectedDto);

        List<ResourceInfoResponseDTO> result = searchService.search(userId, rawQuery);

        assertEquals(1, result.size());
        verify(s3Repo).getResourceByPath(bucket, technicalPaths.getFirst());
    }
}