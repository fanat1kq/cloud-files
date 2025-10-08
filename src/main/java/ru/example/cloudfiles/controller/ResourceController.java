package ru.example.cloudfiles.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.docs.storage.resource.DeleteResourceDocs;
import ru.example.cloudfiles.docs.storage.resource.DownloadResourceDocs;
import ru.example.cloudfiles.docs.storage.resource.GetResourceDocs;
import ru.example.cloudfiles.docs.storage.resource.MoveResourceDocs;
import ru.example.cloudfiles.docs.storage.resource.SearchResourceDocs;
import ru.example.cloudfiles.docs.storage.resource.UploadResourceDocs;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.S3Service;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
@Validated
public class ResourceController {

    private final S3Service s3Service;


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @GetResourceDocs
    public ResourceInfoResponseDTO getResource(
            @RequestParam
            @NotBlank(message = "Parameter \"path\" must not be blank")
            String path,
            @AuthenticationPrincipal
            CustomUserDetails userDetails) {

        return s3Service.getResource(userDetails.getId(), path);
    }


    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteResourceDocs
    public void deleteResource(@RequestParam
                               @NotBlank(message = "Parameter \"path\" must not be blank")
                               String path,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        s3Service.deleteResource(userDetails.getId(), path);
    }

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DownloadResourceDocs
    public ResponseEntity<StreamingResponseBody> downloadResource(
            @RequestParam @NotBlank String path,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        DownloadResult downloadResult =
                s3Service.prepareDownload(userDetails.getId(), path);

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

        return s3Service.moveResource(userDetails.getId(), from, to);
    }


    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @SearchResourceDocs
    public List<ResourceInfoResponseDTO> search(@RequestParam
                                                @NotBlank(message = "Parameter \"query\" must not be blank")
                                                String query,
                                                @AuthenticationPrincipal
                                                CustomUserDetails userDetails) {

        return s3Service.searchResource(userDetails.getId(), query);
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

        return s3Service.uploadResource(userDetails.getId(), path, files);
    }
}