package ru.example.cloudfiles.service;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractMinioTestContainer {

    private static final String BUCKET = "test-bucket";

    @Container
    public static MinIOContainer MINIO_CONTAINER = new MinIOContainer("minio/minio:latest")
            .withUserName("minioTest")
            .withPassword("minioTestPass")
            .withExposedPorts(9000);

    public static String getUsername() {
        return MINIO_CONTAINER.getUserName();
    }

    public static String getPassword() {
        return MINIO_CONTAINER.getPassword();
    }

    public static String getUrl() {
        return MINIO_CONTAINER.getS3URL();
    }




}
