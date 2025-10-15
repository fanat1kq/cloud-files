package ru.example.cloudfiles.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.example.cloudfiles.dto.request.UserRequestDTO;
import ru.example.cloudfiles.entity.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends BaseWebMvcTest {

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