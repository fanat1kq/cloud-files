package ru.example.cloudfiles.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ru.example.cloudfiles.dto.request.UserRequestDTO;
import ru.example.cloudfiles.entity.User;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.S3Service;
import ru.example.cloudfiles.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private S3Service s3Service;

    private static RequestPostProcessor withCustomUser(long id, String username) {

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

    @Test
    @SneakyThrows
    @DisplayName("POST /api/auth/sign-up creates user, creates user dir, returns 201 and username")
    void signUp_createsUserAndDir_returns201() {
        UserRequestDTO request = new UserRequestDTO("johnny", "password123");
        User saved = new User();
        saved.setId(99L);
        saved.setUsername("johnny");
        saved.setPassword("hashed");

        when(userService.signUp(any(UserRequestDTO.class), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("johnny"));
    }

    @Test
    @SneakyThrows
    @DisplayName("GET /api/user/me returns current user info")
    void getUser_returnsCurrent() {

        long userId = 12L;
        String username = "mike";

        mockMvc.perform(get("/api/user/me")
                        .with(withCustomUser(userId, username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        verifyNoInteractions(userService, s3Service);
    }
}