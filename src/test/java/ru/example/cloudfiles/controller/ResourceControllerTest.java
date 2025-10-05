package ru.example.cloudfiles.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.ResourceType;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.service.S3Service;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ResourceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private S3Service s3Service;
    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("GET /api/resource returns resource info")
    void getResource_returnsInfo() throws Exception {
        long userId = 9L;
        String path = "/a.txt";
        var info = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        info = ResourceInfoResponseDTO.builder()
                .path(path)
                .name("a.txt")
                .size(10L)
                .type(ResourceType.FILE)
                .build();
        when(s3Service.getResource(userId, path)).thenReturn(info);

        mockMvc.perform(get("/api/resource")
                        .param("path", path)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.name").value("a.txt"))
                .andExpect(jsonPath("$.type").value("FILE"));
    }

    @Test
    @DisplayName("DELETE /api/resource deletes and returns 204")
    void deleteResource_noContent() throws Exception {
        long userId = 2L;
        String path = "/to-remove";
        doNothing().when(s3Service).deleteResource(userId, path);

        mockMvc.perform(delete("/api/resource")
                        .param("path", path)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "sam")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/resource/download returns 200 and content-disposition header")
    void downloadResource_ok() throws Exception {
        long userId = 3L;
        String path = "/docs/a.pdf";
        StreamingResponseBody body = outputStream -> outputStream.write("bytes".getBytes(StandardCharsets.UTF_8));
        when(s3Service.prepareDownload(userId, path)).thenReturn(new DownloadResult(body, "attachment; filename=a.pdf"));

        mockMvc.perform(get("/api/resource/download")
                        .param("path", path)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "tom"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=a.pdf"));
    }

    @Test
    @DisplayName("GET /api/resource/move returns moved resource info")
    void move_ok() throws Exception {
        long userId = 5L;
        String from = "/a";
        String to = "/b";
        var moved = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        moved = ResourceInfoResponseDTO.builder()
                .path(to)
                .name("b")
                .size(0L)
                .type(ResourceType.DIRECTORY)
                .build();
        when(s3Service.moveResource(userId, from, to)).thenReturn(moved);

        mockMvc.perform(get("/api/resource/move")
                        .param("from", from)
                        .param("to", to)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "kate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(to))
                .andExpect(jsonPath("$.name").value("b"));
    }

    @Test
    @DisplayName("GET /api/resource/search returns list")
    void search_ok() throws Exception {
        long userId = 1L;
        String query = "a";
        var item = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        item = ResourceInfoResponseDTO.builder()
                .path("/a")
                .name("a")
                .size(0L)
                .type(ResourceType.DIRECTORY)
                .build();
        when(s3Service.search(userId, query)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/resource/search")
                        .param("query", query)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "eve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("a"));
    }

    @Test
    @DisplayName("POST /api/resource upload returns created list")
    void upload_ok() throws Exception {
        long userId = 8L;
        String path = "/uploads";
        MockMultipartFile file1 = new MockMultipartFile("object", "a.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());
        var item = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        item = ResourceInfoResponseDTO.builder()
                .path(path + "/a.txt")
                .name("a.txt")
                .size(5L)
                .type(ResourceType.FILE)
                .build();
        when(s3Service.upload(eq(userId), eq(path), any())).thenReturn(List.of(item));

        mockMvc.perform(multipart("/api/resource")
                        .file(file1)
                        .param("path", path)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "neo")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("a.txt"));
    }

    @Test
    @DisplayName("GET /api/resource with Podam data returns resource info")
    void getResource_withPodamData_returnsInfo() throws Exception {
        long userId = 15L;
        var info = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        when(s3Service.getResource(userId, info.path())).thenReturn(info);

        mockMvc.perform(get("/api/resource")
                        .param("path", info.path())
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "podam-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(info.path()))
                .andExpect(jsonPath("$.name").value(info.name()))
                .andExpect(jsonPath("$.type").value(info.type().toString()));
    }

    @Test
    @DisplayName("GET /api/resource/search with Podam data returns list")
    void search_withPodamData_returnsList() throws Exception {
        long userId = 20L;
        String query = "search";
        var item1 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        var item2 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        when(s3Service.search(userId, query)).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/resource/search")
                        .param("query", query)
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Name", "podam-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value(item1.path()))
                .andExpect(jsonPath("$[0].name").value(item1.name()))
                .andExpect(jsonPath("$[1].path").value(item2.path()))
                .andExpect(jsonPath("$[1].name").value(item2.name()));
    }

    @TestConfiguration
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