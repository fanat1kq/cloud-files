package ru.example.cloudfiles.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.docs.storage.resource.*;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.S3UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
@Validated
public class ResourceController {

    private final S3UserService s3UserService;


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @GetResourceDocs
    public ResourceInfoResponseDTO getResource(
            @RequestParam
            @NotBlank(message = "Parameter \"path\" must not be blank")
            String path,
            @AuthenticationPrincipal
            CustomUserDetails userDetails) {

        return s3UserService.getResource(userDetails.getId(), path);
    }


    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteResourceDocs
    public void deleteResource(@RequestParam
                               @NotBlank(message = "Parameter \"path\" must not be blank")
                               String path,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        s3UserService.deleteResource(userDetails.getId(), path);
    }

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DownloadResourceDocs
    public ResponseEntity<StreamingResponseBody> downloadResource(
            @RequestParam @NotBlank String path,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        DownloadResult downloadResult =
                s3UserService.prepareDownload(userDetails.getId(), path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        downloadResult.contentDisposition())
                .body(downloadResult.streamingBody());
    }


    @GetMapping("/move")
    @ResponseStatus(HttpStatus.OK)
    @MoveResourceDocs
    public ResourceInfoResponseDTO move(@RequestParam
                                        @NotBlank(message = "Parameter \"from\" must not be blank")
                                        String from,
                                        @RequestParam
                                        @NotBlank(message = "Parameter \"to\" must not be blank")
                                        String to,
                                        @AuthenticationPrincipal
                                        CustomUserDetails userDetails) {

        return s3UserService.moveResource(userDetails.getId(), from, to);
    }


    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @SearchResourceDocs
    public List<ResourceInfoResponseDTO> search(@RequestParam
                                                @NotBlank(message = "Parameter \"query\" must not be blank")
                                                String query,
                                                @AuthenticationPrincipal
                                                CustomUserDetails userDetails) {

        return s3UserService.search(userDetails.getId(), query);
    }


    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @UploadResourceDocs
    public List<ResourceInfoResponseDTO> upload(
            @RequestParam String path,
            @RequestParam(name = "object")
            MultipartFile[] files,
            @AuthenticationPrincipal
            CustomUserDetails userDetails) {

        return s3UserService.upload(userDetails.getId(), path, files);
    }
}