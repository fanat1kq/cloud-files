package ru.example.cloudfiles.repository;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class AbstractPostgreSQLTestContainer {
    @Container
    public static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:15-alpine");

    public static String getUsername() {
        return POSTGRES_CONTAINER.getUsername();
    }

    public static String getPassword() {
        return POSTGRES_CONTAINER.getPassword();
    }

    public static String getUrl() {
        return POSTGRES_CONTAINER.getJdbcUrl();
    }
}
