package ru.example.cloudfiles.controller;

import jakarta.validation.Valid;
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
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.S3UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
@Validated//TODO
public class ResourceController {

          private final S3UserService s3UserService;


          @GetMapping
          @ResponseStatus(HttpStatus.OK)
          @GetResourceDocs
          public ResourceInfoResponseDTO getResource(@Valid
                                                     @RequestParam(name = "path")
                                                     @NotBlank(message = "Parameter \"path\" must not be blank")
                                                     String path,
                                                     @AuthenticationPrincipal
                                                     CustomUserDetails userDetails)
                    throws Exception {

                    return s3UserService.getResource(userDetails.getId(), path);
          }


          @DeleteMapping
          @ResponseStatus(HttpStatus.NO_CONTENT)
          @DeleteResourceDocs
          public void deleteResource(@Valid
                                     @RequestParam(name = "path")
                                     @NotBlank(message = "Parameter \"path\" must not be blank")
                                     String path,
                                     @AuthenticationPrincipal CustomUserDetails userDetails)
                    throws Exception {

                    s3UserService.deleteResource(userDetails.getId(), path);
          }

          @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
          @ResponseStatus(HttpStatus.ACCEPTED)
          @DownloadResourceDocs
          public ResponseEntity<StreamingResponseBody> downloadResource(@Valid
                                                                        @RequestParam(name = "path")
                                                                        @NotBlank(message = "Parameter \"path\" must not be blank")
                                                                        String path,
                                                                        @AuthenticationPrincipal
                                                                        CustomUserDetails userDetails)
                    throws Exception {

                    StreamingResponseBody streamingResponseBody =
                              s3UserService.downloadResource(userDetails.getId(), path);

                    Path entirePath = Paths.get(path);
                    String fileName;

                    if (path.endsWith("/")) {
                              fileName = entirePath.getFileName().toString().concat(".zip");
                    } else {
                              fileName = entirePath.getFileName().toString();
                    }

                    String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                              .replace("+", "%20");

                    return ResponseEntity.ok()
                              .header(HttpHeaders.CONTENT_DISPOSITION,
                                        "attachment; filename*=UTF-8''%s".formatted(
                                                  encodedFileName))
                              .body(streamingResponseBody);
          }


          @GetMapping("/move")
          @ResponseStatus(HttpStatus.OK)
          @MoveResourceDocs
          public ResourceInfoResponseDTO move(@Valid
                                              @RequestParam(name = "from")
                                              @NotBlank(message = "Parameter \"from\" must not be blank")
                                              String from,
                                              @Valid
                                              @RequestParam(name = "to")
                                              @NotBlank(message = "Parameter \"to\" must not be blank")
                                              String to,
                                              @AuthenticationPrincipal
                                              CustomUserDetails userDetails) throws Exception {

                    return s3UserService.moveResource(userDetails.getId(), from, to);
          }


          @GetMapping("/search")
          @ResponseStatus(HttpStatus.OK)
          @SearchResourceDocs
          public List<ResourceInfoResponseDTO> search(@Valid
                                                      @RequestParam(name = "query")
                                                      @NotBlank(message = "Parameter \"query\" must not be blank")
                                                      String query,
                                                      @AuthenticationPrincipal
                                                      CustomUserDetails userDetails)
                    throws Exception {

                    return s3UserService.searchResource(userDetails.getId(), query);
          }


          @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
          @ResponseStatus(HttpStatus.CREATED)
          @UploadResourceDocs
          public List<ResourceInfoResponseDTO> upload(
                    @RequestParam(name = "path", defaultValue = "") String path,
                    @RequestParam(name = "object")
                    MultipartFile[] files,
                    @AuthenticationPrincipal
                    CustomUserDetails userDetails)
                    throws Exception {

                    return s3UserService.uploadFiles(userDetails.getId(), path, files);
          }
}