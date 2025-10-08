package ru.example.cloudfiles.controller;


import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.cloudfiles.docs.storage.directory.CreateDirectoryDocs;
import ru.example.cloudfiles.docs.storage.directory.GetDirectoriesDocs;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.S3Service;

import java.util.List;


@RestController
@RequestMapping("api/directory")
@RequiredArgsConstructor
@Validated
public class DirectoryController {

    private final S3Service s3Service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @GetDirectoriesDocs
    public List<ResourceInfoResponseDTO> getDirectory(@RequestParam
                                                      String path,
                                                      @AuthenticationPrincipal
                                                      CustomUserDetails userDetails) {

        return s3Service.getDirection(userDetails.getId(), path);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CreateDirectoryDocs
    public ResourceInfoResponseDTO createDirectory(@RequestParam
                                                   @NotBlank(message = "Parameter \"path\" must not be blank")
                                                   String path,
                                                   @AuthenticationPrincipal
                                                   CustomUserDetails userDetails) {

        return s3Service.createDirection(userDetails.getId(), path);
    }
}
