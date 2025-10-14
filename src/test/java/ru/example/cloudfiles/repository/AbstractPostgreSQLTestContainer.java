package ru.example.cloudfiles.repository;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
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

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AbstractPostgreSQLTestContainer::getUrl);
        registry.add("spring.datasource.username", AbstractPostgreSQLTestContainer::getUsername);
        registry.add("spring.datasource.password", AbstractPostgreSQLTestContainer::getPassword);

    }
}
