package ru.example.cloudfiles.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.example.cloudfiles.dto.request.UserRequestDTO;
import ru.example.cloudfiles.dto.response.UserResponseDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseWebMvcTest {

    @Test
    @DisplayName("POST /api/auth/sign-in returns 200 and user info")
    void signIn_returnsUserResponse() throws Exception {

        UserRequestDTO request = factory.manufacturePojo(UserRequestDTO.class);
        UserResponseDTO response = factory.manufacturePojo(UserResponseDTO.class);

        when(userService.signIn(any(UserRequestDTO.class), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(response.username()));
    }
}
