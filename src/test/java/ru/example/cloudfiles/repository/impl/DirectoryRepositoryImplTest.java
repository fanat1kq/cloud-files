package ru.example.cloudfiles.repository.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.example.cloudfiles.repository.impl.composition.DirectoryRepository;
import ru.example.cloudfiles.service.AbstractMinioTestContainer;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.ByteArrayInputStream;

import static org.postgresql.core.Oid.PATH;

@SpringBootTest
@ExtendWith(SoftAssertionsExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "preliquibase.enabled=false"
})
public class DirectoryRepositoryImplTest extends AbstractMinioTestContainer {

    private static final String BUCKET = "test-bucket";

    @MockitoBean
    private MinioClient minioClient;

    @Autowired
    private DirectoryRepository directoryRepository;

    private PodamFactory factory;

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("cloudstorage.storage.minio.endpoint", MINIO_CONTAINER::getS3URL);
        registry.add("cloudstorage.storage.minio.user", MINIO_CONTAINER::getUserName);
        registry.add("cloudstorage.storage.minio.password", MINIO_CONTAINER::getPassword);
        registry.add("cloudstorage.storage.minio.bucket", () -> BUCKET);
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
    public void init() throws Exception {

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Test getting all directories by prefix")
    public void findAllNamesByPrefix(SoftAssertions softly) throws Exception {

        String matchingFile = PATH + "/my-file.txt";
        String otherFile = PATH + "/document.pdf";

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(BUCKET)
                .object(matchingFile)
                .stream(new ByteArrayInputStream("file content".getBytes()), "file content".length(), -1)
                .contentType("text/plain")
                .build());

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(BUCKET)
                .object(otherFile)
                .stream(new ByteArrayInputStream("other content".getBytes()), "other content".length(), -1)
                .contentType("application/pdf")
                .build());

//        List<String> allNamesByPrefix = directoryRepository.findAllNamesByPrefix(BUCKET, "document", false);
//        assertEquals(1, allNamesByPrefix.size());

    }
}
