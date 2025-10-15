package ru.example.cloudfiles.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.S3Service;
import ru.example.cloudfiles.service.UserService;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Для @BeforeAll
public abstract class BaseWebMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected S3Service s3Service;

    protected PodamFactory factory;

    protected static RequestPostProcessor withCustomUser(long id, String username) {
        return request -> {
            CustomUserDetails userDetails = new CustomUserDetails();
            userDetails.setId(id);
            userDetails.setUsername(username);
            userDetails.setPassword("password");

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    "password",
                    userDetails.getAuthorities()
            );

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            return request;
        };
    }

    @BeforeAll
    void setUpFactory() {
        factory = new PodamFactoryImpl();
    }
}
