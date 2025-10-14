package ru.example.cloudfiles.repository;

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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.example.cloudfiles.entity.User;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(SoftAssertionsExtension.class)
@Sql("/test-data.sql")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest extends AbstractPostgreSQLTestContainer {

    @Autowired
    private UserRepository userRepository;

    private PodamFactory factory;

    @BeforeAll
    static void setUp() {
        POSTGRES_CONTAINER.start();
    }

    @AfterAll
    static void afterAll() {
        POSTGRES_CONTAINER.stop();
    }

    @BeforeEach
    void init() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Test getting user by username")
    void findUserByName(SoftAssertions softly) {

        User user = new User();
        user.setUsername(factory.manufacturePojo(String.class));
        user.setPassword(factory.manufacturePojo(String.class));

        User savedUser = userRepository.save(user);

        User found = userRepository.findUserByUsername(user.getUsername())
                .orElseThrow(() -> new AssertionError("User not found"));

        softly.assertThat(found).isNotNull();
        softly.assertThat(found.getId()).isEqualTo(savedUser.getId());
        softly.assertThat(found.getUsername()).isEqualTo(user.getUsername());
    }
}