package ru.example.cloudfiles.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.ResourceType;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;

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

class ResourceControllerTest extends BaseWebMvcTest {

    @Test
    @SneakyThrows
    @DisplayName("GET /api/resource returns resource info")
    void getResource_returnsInfo() {

        long userId = 9L;
        String username = "bob";
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
                        .with(withCustomUser(userId, username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.name").value("a.txt"))
                .andExpect(jsonPath("$.type").value("FILE"));
    }

    @Test
    @SneakyThrows
    @DisplayName("DELETE /api/resource deletes and returns 204")
    void deleteResource_noContent() {

        long userId = 2L;
        String username = "sam";
        String path = "/to-remove";
        doNothing().when(s3Service).deleteResource(userId, path);

        mockMvc.perform(delete("/api/resource")
                        .param("path", path)
                        .with(withCustomUser(userId, username))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    @DisplayName("GET /api/resource/download returns 200 and content-disposition header")
    void downloadResource_ok() {

        long userId = 3L;
        String username = "tom";
        String path = "/docs/a.pdf";
        StreamingResponseBody body = outputStream -> outputStream.write("bytes".getBytes(StandardCharsets.UTF_8));
        when(s3Service.prepareDownload(userId, path)).thenReturn(new DownloadResult(body, "attachment; filename=a.pdf"));

        mockMvc.perform(get("/api/resource/download")
                        .param("path", path)
                        .with(withCustomUser(userId, username)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=a.pdf"));
    }

    @Test
    @SneakyThrows
    @DisplayName("GET /api/resource/move returns moved resource info")
    void move_ok() {

        long userId = 5L;
        String username = "kate";
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
                        .with(withCustomUser(userId, username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(to))
                .andExpect(jsonPath("$.name").value("b"));
    }

    @Test
    @SneakyThrows
    @DisplayName("GET /api/resource/search returns list")
    void search_ok() {

        long userId = 1L;
        String username = "eve";
        String query = "a";
        var item = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        item = ResourceInfoResponseDTO.builder()
                .path("/a")
                .name("a")
                .size(0L)
                .type(ResourceType.DIRECTORY)
                .build();
        when(s3Service.searchResource(userId, query)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/resource/search")
                        .param("query", query)
                        .with(withCustomUser(userId, username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("a"));
    }

    @Test
    @SneakyThrows
    @DisplayName("POST /api/resource upload returns created list")
    void upload_ok() {

        long userId = 8L;
        String username = "neo";
        String path = "/uploads";
        MockMultipartFile file1 = new MockMultipartFile("object", "a.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());
        var item = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        item = ResourceInfoResponseDTO.builder()
                .path(path + "/a.txt")
                .name("a.txt")
                .size(5L)
                .type(ResourceType.FILE)
                .build();
        when(s3Service.uploadResource(eq(userId), eq(path), any())).thenReturn(List.of(item));

        mockMvc.perform(multipart("/api/resource")
                        .file(file1)
                        .param("path", path)
                        .with(withCustomUser(userId, username))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("a.txt"));
    }

    @Test
    @SneakyThrows
    @DisplayName("GET /api/resource with Podam data returns resource info")
    void getResource_withPodamData_returnsInfo() {

        long userId = 15L;
        String username = "podam-user";
        var info = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        when(s3Service.getResource(userId, info.path())).thenReturn(info);

        mockMvc.perform(get("/api/resource")
                        .param("path", info.path())
                        .with(withCustomUser(userId, username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(info.path()))
                .andExpect(jsonPath("$.name").value(info.name()))
                .andExpect(jsonPath("$.type").value(info.type().toString()));
    }

    @Test
    @SneakyThrows
    @DisplayName("GET /api/resource/search with Podam data returns list")
    void search_withPodamData_returnsList() {

        long userId = 20L;
        String username = "podam-user";
        String query = "search";
        var item1 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        var item2 = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        when(s3Service.searchResource(userId, query)).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/resource/search")
                        .param("query", query)
                        .with(withCustomUser(userId, username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value(item1.path()))
                .andExpect(jsonPath("$[0].name").value(item1.name()))
                .andExpect(jsonPath("$[1].path").value(item2.path()))
                .andExpect(jsonPath("$[1].name").value(item2.name()));
    }

}