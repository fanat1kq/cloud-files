package ru.example.cloudfiles.service.impl.composition;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.exception.storageOperation.directory.DirectoryNotExistException;
import ru.example.cloudfiles.exception.storageOperation.resource.ResourceAlreadyExistsException;
import ru.example.cloudfiles.exception.storageOperation.resource.ResourceUploadException;
import ru.example.cloudfiles.mapper.ResourceMapper;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock
    private S3Repository s3Repo;

    @Mock
    private PathManager paths;

    @Mock
    private MinioProperties props;

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private UploadService uploadService;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Should upload files successfully")
    void uploadOk() {

        long userId = factory.manufacturePojo(Long.class);
        String uploadPath = "documents/";
        String bucket = factory.manufacturePojo(String.class);

        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        MultipartFile[] files = {file1, file2};

        String filename1 = "file1.txt";
        String filename2 = "file2.jpg";
        String techPath1 = "user-" + userId + "/documents/file1.txt";
        String techPath2 = "user-" + userId + "/documents/file2.jpg";

        Resource resource1 = factory.manufacturePojo(Resource.class);
        Resource resource2 = factory.manufacturePojo(Resource.class);
        ResourceInfoResponseDTO dto1 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        ResourceInfoResponseDTO dto2 = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        when(props.getBucket()).thenReturn(bucket);
        when(paths.isDirectory(uploadPath)).thenReturn(true);
        when(paths.toTechnicalPath(userId, uploadPath + filename1)).thenReturn(techPath1);
        when(paths.toTechnicalPath(userId, uploadPath + filename2)).thenReturn(techPath2);
        when(paths.toTechnicalPath(userId, uploadPath)).thenReturn("user-" + userId + "/documents/");

        when(file1.getOriginalFilename()).thenReturn(filename1);
        when(file2.getOriginalFilename()).thenReturn(filename2);

        try {
            when(file1.getInputStream()).thenReturn(new ByteArrayInputStream("content1".getBytes()));
            when(file2.getInputStream()).thenReturn(new ByteArrayInputStream("content2".getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        when(s3Repo.isObjectExists(bucket, "user-" + userId + "/documents/")).thenReturn(true);
        when(s3Repo.isObjectExists(bucket, techPath1)).thenReturn(false);
        when(s3Repo.isObjectExists(bucket, techPath2)).thenReturn(false);

        when(s3Repo.getResourceByPath(bucket, techPath1)).thenReturn(resource1);
        when(s3Repo.getResourceByPath(bucket, techPath2)).thenReturn(resource2);

        when(resourceMapper.toDto(userId, resource1)).thenReturn(dto1);
        when(resourceMapper.toDto(userId, resource2)).thenReturn(dto2);

        List<ResourceInfoResponseDTO> result = uploadService.upload(userId, uploadPath, files);

        assertEquals(2, result.size());
        verify(s3Repo, times(2)).saveResource(eq(bucket), anyString(), any(InputStream.class));
        verify(s3Repo, times(2)).getResourceByPath(eq(bucket), anyString());
        verify(resourceMapper, times(2)).toDto(eq(userId), any(Resource.class));
    }

    @Test
    @DisplayName("Should throw DirectoryNotExistException when upload path does not exist")
    void uploadDirectoryNotExist() {

        long userId = factory.manufacturePojo(Long.class);
        String uploadPath = "nonexistent/";
        MultipartFile file = mock(MultipartFile.class);
        MultipartFile[] files = {file};

        when(paths.isDirectory(uploadPath)).thenReturn(true);
        when(s3Repo.isObjectExists(any(), any())).thenReturn(false);

        assertThrows(DirectoryNotExistException.class, () -> uploadService.upload(userId, uploadPath, files));
        verify(s3Repo, never()).saveResource(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw ResourceAlreadyExistsException when file already exists")
    void uploadFileAlreadyExists() {

        long userId = factory.manufacturePojo(Long.class);
        String uploadPath = "documents/";
        String filename = "existing.txt";
        MultipartFile file = mock(MultipartFile.class);
        MultipartFile[] files = {file};

        when(paths.isDirectory(uploadPath)).thenReturn(true);
        when(file.getOriginalFilename()).thenReturn(filename);
        when(s3Repo.isObjectExists(any(), any())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> uploadService.upload(userId, uploadPath, files));
        verify(s3Repo, never()).saveResource(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw ResourceUploadException when file upload fails")
    @SneakyThrows
    void uploadFileFails() {

        long userId = factory.manufacturePojo(Long.class);
        String uploadPath = "documents/";
        String filename = "file.txt";
        MultipartFile file = mock(MultipartFile.class);
        MultipartFile[] files = {file};

        when(paths.isDirectory(uploadPath)).thenReturn(true);
        when(file.getOriginalFilename()).thenReturn(filename);
        when(s3Repo.isObjectExists(any(), any())).thenReturn(true, false);

        when(file.getInputStream()).thenThrow(new IOException("File read error"));

        assertThrows(ResourceUploadException.class, () -> uploadService.upload(userId, uploadPath, files));
    }

    @Test
    @DisplayName("Should handle file with null filename")
    void uploadNullFilename() {

        long userId = factory.manufacturePojo(Long.class);
        String uploadPath = "documents/";
        MultipartFile file = mock(MultipartFile.class);
        MultipartFile[] files = {file};

        when(paths.isDirectory(uploadPath)).thenReturn(true);
        when(file.getOriginalFilename()).thenReturn(null);
        when(s3Repo.isObjectExists(any(), any())).thenReturn(true);

        assertThrows(NullPointerException.class, () -> uploadService.upload(userId, uploadPath, files));
    }

    @Test
    @DisplayName("Should validate upload path is directory")
    void uploadPathNotDirectory() {

        long userId = factory.manufacturePojo(Long.class);
        String uploadPath = "not_a_directory";
        MultipartFile file = mock(MultipartFile.class);
        MultipartFile[] files = {file};

        when(paths.isDirectory(uploadPath)).thenReturn(false);

        assertThrows(DirectoryNotExistException.class, () -> uploadService.upload(userId, uploadPath, files));
    }
}