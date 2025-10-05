package ru.example.cloudfiles.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.example.cloudfiles.dto.request.UserRequestDTO;
import ru.example.cloudfiles.dto.response.UserResponseDTO;
import ru.example.cloudfiles.entity.User;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.AuthService;
import ru.example.cloudfiles.service.S3UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class TestAuthPrincipalResolver implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(java.util.List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(org.springframework.security.core.annotation.AuthenticationPrincipal.class)
                            && parameter.getParameterType().isAssignableFrom(ru.example.cloudfiles.security.CustomUserDetails.class);
                }

                @Override
                public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                              org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                              org.springframework.web.context.request.NativeWebRequest webRequest,
                                              org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                    String idHeader = webRequest.getHeader("X-User-Id");
                    String nameHeader = webRequest.getHeader("X-User-Name");
                    long id = idHeader != null ? Long.parseLong(idHeader) : 1L;
                    String name = nameHeader != null ? nameHeader : "test";
                    ru.example.cloudfiles.security.CustomUserDetails cud = new ru.example.cloudfiles.security.CustomUserDetails();
                    cud.setId(id);
                    cud.setUsername(name);
                    cud.setPassword("nop");
                    return cud;
                }
            });
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private S3UserService s3UserService;

    @Test
    @DisplayName("POST /api/auth/sign-up creates user, creates user dir, returns 201 and username")
    void signUp_createsUserAndDir_returns201() throws Exception {
        UserRequestDTO request = new UserRequestDTO("johnny", "password123");
        User saved = new User();
        saved.setId(99L);
        saved.setUsername("johnny");
        saved.setPassword("hashed");

        when(authService.signUp(any(UserRequestDTO.class), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("johnny"));

        verify(s3UserService).createUserDir(99L);
    }

    @Test
    @DisplayName("GET /api/user/me returns current user info")
    void getUser_returnsCurrent() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .header("X-User-Id", "12")
                        .header("X-User-Name", "mike"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("mike"));
    }
}
