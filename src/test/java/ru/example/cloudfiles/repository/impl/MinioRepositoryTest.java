package ru.example.cloudfiles.repository.impl;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.repository.AbstractMinioTestContainer;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class MinioRepositoryTest extends AbstractMinioTestContainer {

    private static final String BUCKET = "test-bucket";
    private final MinioClient minioClient;
    private final MinioRepository minioRepository;
    private PodamFactory factory;

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", AbstractMinioTestContainer::getUrl);
        registry.add("minio.access-key", AbstractMinioTestContainer::getUsername);
        registry.add("minio.secret-key", AbstractMinioTestContainer::getPassword);
        registry.add("minio.bucket", () -> BUCKET);
    }

    @BeforeAll
    static void setUp() {
        MINIO_CONTAINER.start();
    }

    @AfterAll
    static void afterAll() {
        MINIO_CONTAINER.stop();
    }

    @BeforeEach
    @SneakyThrows
    void init() {

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }
        factory = new PodamFactoryImpl();
    }

    @AfterEach
    void cleanup() {
        clearBucket();
    }

    @Test
    @DisplayName("Should delete single object")
    void shouldDeleteSingleObject() {

        String objectPath = factory.manufacturePojo(String.class);
        createEmptyObject(objectPath);

        minioRepository.deleteResource(BUCKET, objectPath);

        assertThatObjectDoesNotExist(objectPath);
    }

    @Test
    @DisplayName("Should delete directory recursively")
    void shouldDeleteDirectoryRecursively() {

        String baseDir = factory.manufacturePojo(String.class) + "/";
        String file1 = baseDir + factory.manufacturePojo(String.class);
        String file2 = baseDir + factory.manufacturePojo(String.class);

        createEmptyObject(file1);
        createEmptyObject(file2);

        minioRepository.deleteResource(BUCKET, baseDir);

        assertThatObjectDoesNotExist(file1);
        assertThatObjectDoesNotExist(file2);
    }

    @Test
    @DisplayName("Should save and get resource")
    void shouldSaveAndGetResource() {

        String objectPath = factory.manufacturePojo(String.class);
        String content = "test content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        minioRepository.saveResource(BUCKET, objectPath, inputStream);
        Resource resource = minioRepository.getResourceByPath(BUCKET, objectPath);

        assertThat(resource).isNotNull();
        assertThat(minioRepository.isObjectExists(BUCKET, objectPath)).isTrue();
    }

    @Test
    @DisplayName("Should create directory")
    void shouldCreateDirectory() {

        String directoryPath = factory.manufacturePojo(String.class) + "/";

        minioRepository.createDirectory(BUCKET, directoryPath);

        assertThatDirectoryExists(directoryPath);
    }

    @Test
    @DisplayName("Should check if object exists")
    void shouldCheckIfObjectExists() {

        String objectPath = factory.manufacturePojo(String.class);
        createEmptyObject(objectPath);

        boolean exists = minioRepository.isObjectExists(BUCKET, objectPath);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should find all names by prefix")
    void shouldFindAllNamesByPrefix() {

        String prefix = factory.manufacturePojo(String.class) + "/";
        String file1 = prefix + factory.manufacturePojo(String.class);
        String file2 = prefix + factory.manufacturePojo(String.class);

        createEmptyObject(file1);
        createEmptyObject(file2);

        List<String> names = minioRepository.findAllNamesByPrefix(BUCKET, prefix, true);

        assertThat(names).contains(file1, file2);
    }

    @Test
    @DisplayName("Should handle non-existing object check")
    void shouldHandleNonExistingObjectCheck() {

        String nonExistingPath = factory.manufacturePojo(String.class);

        boolean exists = minioRepository.isObjectExists(BUCKET, nonExistingPath);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should handle nested directory deletion")
    void shouldHandleNestedDirectoryDeletion() {

        String baseDir = factory.manufacturePojo(String.class) + "/";
        String subDir = baseDir + factory.manufacturePojo(String.class) + "/";
        String file1 = subDir + factory.manufacturePojo(String.class);
        String file2 = baseDir + factory.manufacturePojo(String.class);

        createEmptyObject(file1);
        createEmptyObject(file2);

        minioRepository.deleteResource(BUCKET, baseDir);

        assertThatObjectDoesNotExist(file1);
        assertThatObjectDoesNotExist(file2);
    }

    private void assertThatDirectoryExists(String directoryPath) {

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(directoryPath)
                            .build()
            );
        } catch (Exception e) {
            throw new AssertionError("Directory " + directoryPath + " should exist", e);
        }
    }

    private void assertThatObjectDoesNotExist(String objectPath) {

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(objectPath)
                            .build()
            );
            throw new AssertionError("Object " + objectPath + " should not exist");
        } catch (ErrorResponseException e) {
            assertThat(e.errorResponse().code()).isEqualTo("NoSuchKey");
        } catch (Exception e) {
            throw new AssertionError("Unexpected error when checking object existence", e);
        }
    }

    @SneakyThrows
    private void createEmptyObject(String objectPath) {

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(objectPath)
                        .stream(InputStream.nullInputStream(), 0, -1)
                        .build()
        );
    }

    @SneakyThrows
    private void clearBucket() {

        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(BUCKET).recursive(true).build()
        );

        for (Result<Item> result : objects) {
            Item item = result.get();
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(item.objectName())
                            .build()
            );
        }
    }
}