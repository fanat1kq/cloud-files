//package ru.example.cloudfiles.service;
//
//import io.minio.BucketExistsArgs;
//import io.minio.MakeBucketArgs;
//import io.minio.MinioClient;
//import io.minio.PutObjectArgs;
//import io.minio.StatObjectArgs;
//import io.minio.errors.ErrorResponseException;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.context.TestPropertySource;
//import org.testcontainers.containers.MinIOContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.io.ByteArrayInputStream;
//import java.util.List;
//
//import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//@Testcontainers
//@TestPropertySource("classpath:application-test.yml")
//@SpringBootTest
//@AutoConfigureMockMvc
//public class StorageTest {
//    private static final String BUCKET = "test-bucket";
//    private static final String PATH = "test-folder";
//    private static final int USER_ID = 123;
//
//    @Container
//    static MinIOContainer minioContainer = new MinIOContainer("minio/minio:latest")
//            .withUserName("minioTest")
//            .withPassword("minioTestPass")
//            .withExposedPorts(9000);
//
//    @Autowired
//    private MinioClient minioClient;
//
//    @Autowired
//    private StorageService storageService;
//
//    @DynamicPropertySource
//    static void minioProperties(DynamicPropertyRegistry registry) {
//        registry.add("cloudstorage.storage.minio.endpoint", minioContainer::getS3URL);
//        registry.add("cloudstorage.storage.minio.user", minioContainer::getUserName);
//        registry.add("cloudstorage.storage.minio.password", minioContainer::getPassword);
//        registry.add("cloudstorage.storage.minio.bucket", () -> BUCKET);
//    }
//
//    @BeforeEach
//    void setUp() throws Exception {
//        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
//            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
//        }
//        ;
//    }
//
//    @AfterEach
//    void cleanUp() {
//        storageService.delete(PATH, USER_ID);
//    }
//
//    @Test
//    void testCreateDirectory_Success() {
//        ResourceResponse response = storageService.createDirectory(PATH, USER_ID);
//        assertNotNull(response);
//        assertTrue(response.name().contains(PATH)); // если record
//    }
//
//    @Test
//    void testCreateDirectory_AlreadyExists() {
//        storageService.createDirectory(PATH, USER_ID);
//
//        StorageException ex = assertThrows(StorageException.class, () ->
//                storageService.createDirectory(PATH, USER_ID));
//
//        assertEquals("Directory already exists", ex.getMessage());
//    }
//
//    @Test
//    void testMoveDirectory_Success() throws Exception {
//        String fromPath = "dir-to-move/";
//        String toPath = "moved-dir/";
//
//        storageService.createDirectory(fromPath, USER_ID);
//
//        String fileName = "test.txt";
//        String filePath = fromPath + fileName;
//        minioClient.putObject(PutObjectArgs.builder()
//                .bucket(BUCKET)
//                .object(filePath)
//                .stream(new ByteArrayInputStream("hello".getBytes()), "hello".length(), -1)
//                .contentType("text/plain")
//                .build());
//
//        ResourceResponse response = storageService.move(fromPath, toPath, USER_ID);
//
//        assertNotNull(response);
//        assertEquals("moved-dir", response.name());
//
//        boolean oldExists = minioClient.statObject(StatObjectArgs.builder()
//                .bucket(BUCKET)
//                .object(filePath)
//                .build()) != null;
//        Assertions.assertThrows(ErrorResponseException.class, () -> {
//            minioClient.statObject(StatObjectArgs.builder()
//                    .bucket(BUCKET)
//                    .object(filePath)
//                    .build());
//        });
//    }
//
//    @Test
//    void testSearchObjectByQuery_Success() throws Exception {
//        storageService.createDirectory(PATH, USER_ID);
//
//        String matchingFile = PATH + "/my-file.txt";
//        String otherFile = PATH + "/document.pdf";
//
//        minioClient.putObject(PutObjectArgs.builder()
//                .bucket(BUCKET)
//                .object(matchingFile)
//                .stream(new ByteArrayInputStream("file content".getBytes()), "file content".length(), -1)
//                .contentType("text/plain")
//                .build());
//
//        minioClient.putObject(PutObjectArgs.builder()
//                .bucket(BUCKET)
//                .object(otherFile)
//                .stream(new ByteArrayInputStream("other content".getBytes()), "other content".length(), -1)
//                .contentType("application/pdf")
//                .build());
//
//        List<ResourceResponse> results = storageService.search("my", USER_ID);
//
//        assertEquals(1, results.size());
//        assertEquals("my-file.txt", results.get(0).name());
//    }
//
//
//}
