package ru.example.cloudfiles.repository.impl.composition;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.messages.Item;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.example.cloudfiles.dto.Resource;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceSaveException;
import ru.example.cloudfiles.repository.AbstractMinioTestContainer;
import ru.example.cloudfiles.validation.PathValidator;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ActiveProfiles({"test", "test-minio"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ObjectRepositoryTest extends AbstractMinioTestContainer {

    private static final String BUCKET = "test-bucket";

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ObjectRepository objectRepository;

    @MockitoBean
    private PathValidator pathValidator;

    private PodamFactory factory;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
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

        doNothing().when(pathValidator).validatePath(anyString());
    }

    @AfterEach
    @SneakyThrows
    void cleanup() {
        clearBucket();
    }

    @Test
    @DisplayName("Should get resource by path and return valid DTO")
    @SneakyThrows
    void shouldGetResourceByPathAndReturnValidDTO() {

        String objectPath = factory.manufacturePojo(String.class);
        String content = factory.manufacturePojo(String.class);
        createObject(objectPath, content);

        Resource resource = objectRepository.getResourceByPath(BUCKET, objectPath);

        assertThat(resource)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.path()).isEqualTo(objectPath);
                    assertThat(r.size()).isEqualTo(content.getBytes().length);
                    assertThat(r.dataStream()).isNotNull();
                });

        @Cleanup InputStream dataStream = resource.dataStream();
        String actualContent = new String(dataStream.readAllBytes());
        assertThat(actualContent).isEqualTo(content);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when resource not exists")
    void shouldThrowResourceNotFoundExceptionWhenResourceNotExists() {

        String nonExistingPath = factory.manufacturePojo(String.class);

        assertThatThrownBy(() -> objectRepository.getResourceByPath(BUCKET, nonExistingPath))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(nonExistingPath);
    }

    @Test
    @DisplayName("Should save resource successfully")
    void shouldSaveResourceSuccessfully() {

        String objectPath = factory.manufacturePojo(String.class);
        String content = factory.manufacturePojo(String.class);
        InputStream dataStream = new ByteArrayInputStream(content.getBytes());

        objectRepository.saveResource(BUCKET, objectPath, dataStream);

        assertThatObjectExists(objectPath);

        Resource savedResource = objectRepository.getResourceByPath(BUCKET, objectPath);
        assertThat(savedResource.size()).isEqualTo(content.getBytes().length);
    }

    @Test
    @DisplayName("Should throw ResourceSaveException on save failure")
    @SneakyThrows
    void shouldThrowResourceSaveExceptionOnSaveFailure() {
        String objectPath = factory.manufacturePojo(String.class);

        @Cleanup
        InputStream invalidStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated IO error");
            }
        };

        assertThatThrownBy(() -> objectRepository.saveResource(BUCKET, objectPath, invalidStream))
                .isInstanceOf(ResourceSaveException.class)
                .hasMessageContaining(objectPath);
    }

    @Test
    @DisplayName("Should return true when object exists")
    void shouldReturnTrueWhenObjectExists() {

        String objectPath = factory.manufacturePojo(String.class);
        createObject(objectPath, factory.manufacturePojo(String.class));

        boolean exists = objectRepository.isObjectExists(BUCKET, objectPath);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when object not exists")
    void shouldReturnFalseWhenObjectNotExists() {

        String nonExistingPath = factory.manufacturePojo(String.class);

        boolean exists = objectRepository.isObjectExists(BUCKET, nonExistingPath);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should handle empty file")
    @SneakyThrows
    void shouldHandleEmptyFile() {

        String objectPath = factory.manufacturePojo(String.class);
        createObject(objectPath, "");

        Resource resource = objectRepository.getResourceByPath(BUCKET, objectPath);

        assertThat(resource)
                .satisfies(r -> {
                    assertThat(r.path()).isEqualTo(objectPath);
                    assertThat(r.size()).isZero();

                    @Cleanup InputStream dataStream = r.dataStream();
                    assertThat(dataStream.read()).isEqualTo(-1);
                });
    }

    @Test
    @DisplayName("Should handle binary data")
    @SneakyThrows
    void shouldHandleBinaryData() {

        String objectPath = factory.manufacturePojo(String.class);
        byte[] binaryData = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
        createObjectWithBytes(objectPath, binaryData);

        Resource resource = objectRepository.getResourceByPath(BUCKET, objectPath);

        assertThat(resource)
                .satisfies(r -> {
                    assertThat(r.size()).isEqualTo(binaryData.length);
                    @Cleanup InputStream dataStream = r.dataStream();
                    byte[] readData = dataStream.readAllBytes();
                    assertThat(readData).isEqualTo(binaryData);
                });
    }

    @SneakyThrows
    private void createObject(String objectPath, String content) {

        createObjectWithBytes(objectPath, content.getBytes());
    }

    @SneakyThrows
    private void createObjectWithBytes(String objectPath, byte[] data) {

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(objectPath)
                        .stream(new ByteArrayInputStream(data), data.length, -1)
                        .build()
        );
    }

    private void assertThatObjectExists(String objectPath) {

        assertThatCode(() ->
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(BUCKET)
                                .object(objectPath)
                                .build()
                )
        ).as("Object '%s' should exist", objectPath)
                .doesNotThrowAnyException();
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