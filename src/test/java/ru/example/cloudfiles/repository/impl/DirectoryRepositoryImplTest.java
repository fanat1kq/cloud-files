package ru.example.cloudfiles.repository.impl;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.messages.Item;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import ru.example.cloudfiles.repository.impl.composition.DirectoryRepository;
import ru.example.cloudfiles.service.AbstractMinioTestContainer;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.InputStream;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "preliquibase.enabled=false"
})
public class DirectoryRepositoryImplTest extends AbstractMinioTestContainer {

    private static final String BUCKET = "test-bucket";

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private DirectoryRepository directoryRepository;

    private PodamFactory factory;

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", AbstractMinioTestContainer::getUrl);
        registry.add("minio.access-key", AbstractMinioTestContainer::getUsername);
        registry.add("minio.secret-key", AbstractMinioTestContainer::getPassword);
        registry.add("minio.bucket", () -> BUCKET);
    }

    @BeforeAll
    public static void setUp() {
        MINIO_CONTAINER.start();
    }

    @AfterAll
    public static void afterAll() {
        MINIO_CONTAINER.stop();
    }

    @BeforeEach
    @SneakyThrows
    public void init() {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }
        factory = new PodamFactoryImpl();
    }

    @AfterEach
    public void cleanup() {
        clearBucket();
    }

    @Test
    @DisplayName("Should find all directories by prefix recursively")
    public void shouldFindAllDirectoriesByPrefixRecursively() {

        String basePath = factory.manufacturePojo(String.class) + "/";
        String year = factory.manufacturePojo(String.class) + "/";
        String month1 = factory.manufacturePojo(String.class) + "/";
        String month2 = factory.manufacturePojo(String.class) + "/";
        String documentsPath = factory.manufacturePojo(String.class) + "/";

        directoryRepository.createDirectory(BUCKET, basePath + year + month1);
        directoryRepository.createDirectory(BUCKET, basePath + year + month2);
        directoryRepository.createDirectory(BUCKET, documentsPath + year);

        List<String> resultDirs = directoryRepository.findAllNamesByPrefix(BUCKET, basePath, true);

        assertThat(resultDirs)
                .isNotEmpty()
                .containsExactlyInAnyOrder(
                        basePath,
                        basePath + year,
                        basePath + year + month1,
                        basePath + year + month2
                );
    }

    @Test
    @DisplayName("Should create directory with all parent directories")
    public void shouldCreateDirectoryWithAllParents() {

        String level1 = factory.manufacturePojo(String.class) + "/";
        String level2 = factory.manufacturePojo(String.class) + "/";
        String level3 = factory.manufacturePojo(String.class) + "/";
        String path = level1 + level2 + level3;

        directoryRepository.createDirectory(BUCKET, path);

        assertThatDirectoryExists(level1);
        assertThatDirectoryExists(level1 + level2);
        assertThatDirectoryExists(level1 + level2 + level3);
    }

    @Test
    @DisplayName("Should not create duplicate directories")
    public void shouldNotCreateDuplicateDirectories() {

        String existingBase = factory.manufacturePojo(String.class) + "/";
        String existingDir = factory.manufacturePojo(String.class) + "/";
        String path = existingBase + existingDir;

        createEmptyObject(existingBase);
        createEmptyObject(existingBase + existingDir);

        directoryRepository.createDirectory(BUCKET, path);

        long dirCount = countObjectsWithPrefix(existingBase);
        assertThat(dirCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should extract all directories from path")
    public void shouldExtractAllDirectoriesFromPath() {

        String a = factory.manufacturePojo(String.class) + "/";
        String b = factory.manufacturePojo(String.class) + "/";
        String c = factory.manufacturePojo(String.class) + "/";
        String d = factory.manufacturePojo(String.class) + "/";
        String path = a + b + c + d;

        directoryRepository.createDirectory(BUCKET, path);

        List<String> allDirs = directoryRepository.findAllNamesByPrefix(BUCKET, a, true);
        assertThat(allDirs).containsExactlyInAnyOrder(
                a,
                a + b,
                a + b + c,
                a + b + c + d
        );
    }

    @Test
    @DisplayName("Should handle root directory creation")
    public void shouldHandleRootDirectoryCreation() {

        String singleDir = factory.manufacturePojo(String.class) + "/";

        directoryRepository.createDirectory(BUCKET, singleDir);

        assertThatDirectoryExists(singleDir);
    }

    @Test
    @DisplayName("Should be empty when directory not found")
    public void shouldBeEmptyWhenDirectoryNotFound() {

        String nonExistingPrefix = factory.manufacturePojo(String.class) + "/";

        List<String> result = directoryRepository.findAllNamesByPrefix(BUCKET, nonExistingPrefix, true);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle complex nested directory structure")
    public void shouldHandleComplexNestedDirectoryStructure() {

        String project = factory.manufacturePojo(String.class) + "/";
        String src = factory.manufacturePojo(String.class) + "/";
        String main = factory.manufacturePojo(String.class) + "/";
        String java = factory.manufacturePojo(String.class) + "/";
        String com = factory.manufacturePojo(String.class) + "/";
        String example = factory.manufacturePojo(String.class) + "/";
        String service = factory.manufacturePojo(String.class) + "/";

        String complexPath = project + src + main + java + com + example + service;

        directoryRepository.createDirectory(BUCKET, complexPath);

        assertThatDirectoryExists(project);
        assertThatDirectoryExists(project + src);
        assertThatDirectoryExists(project + src + main);
        assertThatDirectoryExists(project + src + main + java);
        assertThatDirectoryExists(project + src + main + java + com);
        assertThatDirectoryExists(project + src + main + java + com + example);
        assertThatDirectoryExists(complexPath);
    }

    @Test
    @DisplayName("Should find directories non-recursively")
    void shouldFindDirectoriesNonRecursively() {

        String root = factory.manufacturePojo(String.class) + "/";
        String sub1 = factory.manufacturePojo(String.class) + "/";
        String sub2 = factory.manufacturePojo(String.class) + "/";

        directoryRepository.createDirectory(BUCKET, root + sub1);
        directoryRepository.createDirectory(BUCKET, root + sub2);


        List<String> result = directoryRepository.findAllNamesByPrefix(BUCKET, root, false);

        assertThat(result).containsExactlyInAnyOrder(
                root,
                root + sub1,
                root + sub2
        );
    }

    @Test
    @DisplayName("Should handle special characters in directory names")
    void shouldHandleSpecialCharactersInDirectoryNames() {

        String dirWithSpaces = factory.manufacturePojo(String.class) + " with spaces/";
        String dirWithDashes = factory.manufacturePojo(String.class) + "-with-dashes/";
        String dirWithDots = factory.manufacturePojo(String.class) + ".with.dots/";

        String path = dirWithSpaces + dirWithDashes + dirWithDots;

        directoryRepository.createDirectory(BUCKET, path);

        assertThatDirectoryExists(dirWithSpaces);
        assertThatDirectoryExists(dirWithSpaces + dirWithDashes);
        assertThatDirectoryExists(dirWithSpaces + dirWithDashes + dirWithDots);
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

    private long countObjectsWithPrefix(String prefix) {
        return StreamSupport.stream(
                minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(BUCKET)
                                .prefix(prefix)
                                .recursive(true)
                                .build()
                ).spliterator(), false
        ).count();
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