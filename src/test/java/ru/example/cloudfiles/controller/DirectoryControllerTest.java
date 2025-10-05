package ru.example.cloudfiles.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.example.cloudfiles.dto.ResourceType;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.S3UserService;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DirectoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class DirectoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private S3UserService s3UserService;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    private CustomUserDetails buildUser(long id, String username) {
        CustomUserDetails cud = factory.manufacturePojo(CustomUserDetails.class);
        cud.setId(id);
        cud.setUsername(username);
        cud.setPassword("nop");
        return cud;
    }

    @Test
    @DisplayName("GET /api/directory returns directory listing for authenticated user")
    void getDirectory_returnsList() throws Exception {
        String path = "/docs";
        long userId = 42L;

        var item1 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        item1 = ResourceInfoResponseDTO.builder()
                .path("/docs")
                .name("docs")
                .size(0L)
                .type(ResourceType.DIRECTORY)
                .build();

        var item2 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        item2 = ResourceInfoResponseDTO.builder()
                .path("/docs/readme.txt")
                .name("readme.txt")
                .size(128L)
                .type(ResourceType.FILE)
                .build();

        when(s3UserService.getDir(eq(userId), eq(path))).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/directory")
                        .param("path", path)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value("/docs"))
                .andExpect(jsonPath("$[0].name").value("docs"))
                .andExpect(jsonPath("$[0].size").value(0))
                .andExpect(jsonPath("$[0].type").value("DIRECTORY"))
                .andExpect(jsonPath("$[1].name").value("readme.txt"))
                .andExpect(jsonPath("$[1].type").value("FILE"));
    }

    @Test
    @DisplayName("POST /api/directory creates directory and returns its info")
    void createDirectory_createsAndReturnsResource() throws Exception {
        String path = "pics/new";
        long userId = 7L;

        var created = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        created = ResourceInfoResponseDTO.builder()
                .path(path)
                .name("new")
                .size(0L)
                .type(ResourceType.DIRECTORY)
                .build();

        when(s3UserService.createDir(eq(userId), eq(path))).thenReturn(created);

        mockMvc.perform(post("/api/directory")
                        .param("path", path)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "alice")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.name").value("new"))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }

    @Test
    @DisplayName("POST /api/directory with blank path returns 500 Internal Server Error (as per current handler)")
    void createDirectory_blankPath_returnsInternalServerError() throws Exception {
        mockMvc.perform(post("/api/directory")
                        .param("path", " ")
                        .header("X-User-Id", String.valueOf(1L))
                        .header("X-User-Name", "bob")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/directory with Podam-generated data returns directory listing")
    void getDirectory_withPodamData_returnsList() throws Exception {
        String path = "/documents";
        long userId = 123L;

        var item1 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        var item2 = factory.manufacturePojo(ResourceInfoResponseDTO.class);

        item1 = ResourceInfoResponseDTO.builder()
                .path(item1.path())
                .name(item1.name())
                .size(item1.size())
                .type(ResourceType.DIRECTORY)
                .build();

        item2 = ResourceInfoResponseDTO.builder()
                .path(item2.path())
                .name(item2.name())
                .size(item2.size())
                .type(ResourceType.FILE)
                .build();

        when(s3UserService.getDir(eq(userId), eq(path))).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/directory")
                        .param("path", path)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "podam-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value(item1.path()))
                .andExpect(jsonPath("$[0].name").value(item1.name()))
                .andExpect(jsonPath("$[0].type").value("DIRECTORY"))
                .andExpect(jsonPath("$[1].path").value(item2.path()))
                .andExpect(jsonPath("$[1].name").value(item2.name()))
                .andExpect(jsonPath("$[1].type").value("FILE"));
    }

    @Test
    @DisplayName("POST /api/directory with Podam data creates directory")
    void createDirectory_withPodamData_createsAndReturnsResource() throws Exception {
        String path = "photos/vacation";
        long userId = 456L;

        var created = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        created = ResourceInfoResponseDTO.builder()
                .path(path)
                .name(created.name())
                .size(created.size())
                .type(ResourceType.DIRECTORY)
                .build();

        when(s3UserService.createDir(eq(userId), eq(path))).thenReturn(created);

        mockMvc.perform(post("/api/directory")
                        .param("path", path)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "podam-user")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.name").value(created.name()))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }

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
}