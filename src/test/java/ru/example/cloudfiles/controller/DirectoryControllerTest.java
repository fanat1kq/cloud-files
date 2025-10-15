package ru.example.cloudfiles.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.example.cloudfiles.dto.ResourceType;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DirectoryControllerTest extends BaseWebMvcTest {

    @Test
    @SneakyThrows
    @DisplayName("GET /api/directory returns directory listing for authenticated user")
    void getDirectory_returnsList() {

        String path = "/docs";
        long userId = 42L;
        String username = "john";

        var item1 = ResourceInfoResponseDTO.builder()
                .path("/docs")
                .name("docs")
                .size(0L)
                .type(ResourceType.DIRECTORY)
                .build();

        var item2 = ResourceInfoResponseDTO.builder()
                .path("/docs/readme.txt")
                .name("readme.txt")
                .size(128L)
                .type(ResourceType.FILE)
                .build();

        when(s3Service.getDirectory(eq(userId), eq(path))).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/directory")
                        .param("path", path)
                        .with(withCustomUser(userId, username))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value("/docs"))
                .andExpect(jsonPath("$[0].name").value("docs"))
                .andExpect(jsonPath("$[0].size").value(0))
                .andExpect(jsonPath("$[0].type").value("DIRECTORY"))
                .andExpect(jsonPath("$[1].name").value("readme.txt"))
                .andExpect(jsonPath("$[1].type").value("FILE"));
    }

    @Test
    @SneakyThrows
    @DisplayName("POST /api/directory creates directory and returns its info")
    void createDirectory_createsAndReturnsResource() {

        String path = "pics/new";
        long userId = 7L;
        String username = "alice";

        var created = ResourceInfoResponseDTO.builder()
                .path(path)
                .name("new")
                .size(0L)
                .type(ResourceType.DIRECTORY)
                .build();

        when(s3Service.createDirectory(eq(userId), eq(path))).thenReturn(created);

        mockMvc.perform(post("/api/directory")
                        .param("path", path)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(withCustomUser(userId, username))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.name").value("new"))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }

    @Test
    @SneakyThrows
    @DisplayName("POST /api/directory with blank path returns 500 Internal Server Error")
    void createDirectory_blankPath_returnsInternalServerError() {

        mockMvc.perform(post("/api/directory")
                        .param("path", " ")
                        .with(withCustomUser(1L, "bob"))
                        .with(csrf()))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(s3Service);
    }

    @Test
    @SneakyThrows
    @DisplayName("GET /api/directory with Podam-generated data returns directory listing")
    void getDirectory_withPodamData_returnsList() {

        String path = "/documents";
        long userId = 123L;
        String username = "podam-user";

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

        when(s3Service.getDirectory(eq(userId), eq(path))).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/directory")
                        .param("path", path)
                        .with(withCustomUser(userId, username))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value(item1.path()))
                .andExpect(jsonPath("$[0].name").value(item1.name()))
                .andExpect(jsonPath("$[0].type").value("DIRECTORY"))
                .andExpect(jsonPath("$[1].path").value(item2.path()))
                .andExpect(jsonPath("$[1].name").value(item2.name()))
                .andExpect(jsonPath("$[1].type").value("FILE"));
    }

    @Test
    @SneakyThrows
    @DisplayName("POST /api/directory with Podam data creates directory")
    void createDirectory_withPodamData_createsAndReturnsResource() {

        String path = "photos/vacation";
        long userId = 456L;
        String username = "podam-user";

        var created = factory.manufacturePojo(ResourceInfoResponseDTO.class);
        created = ResourceInfoResponseDTO.builder()
                .path(path)
                .name(created.name())
                .size(created.size())
                .type(ResourceType.DIRECTORY)
                .build();

        when(s3Service.createDirectory(eq(userId), eq(path))).thenReturn(created);

        mockMvc.perform(post("/api/directory")
                        .param("path", path)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(withCustomUser(userId, username))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.name").value(created.name()))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }
}