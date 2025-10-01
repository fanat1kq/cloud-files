package ru.example.cloudfiles.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.cloudfiles.docs.storage.directory.CreateDirectoryDocs;
import ru.example.cloudfiles.docs.storage.directory.GetDirectoriesDocs;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.S3UserService;

import java.util.List;


@RestController
@RequestMapping("api/directory")
@RequiredArgsConstructor
@Validated //TODO
public class DirectoryController {

          private final S3UserService s3UserService;

          @InitBinder
          public void initBinder(WebDataBinder binder) {
                    binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
          }


          @GetMapping
          @ResponseStatus(HttpStatus.OK)
          @GetDirectoriesDocs
          public List<ResourceInfoResponseDTO> getDirectory(@RequestParam(name = "path")
                                                            String path,
                                                            @AuthenticationPrincipal
                                                            CustomUserDetails userDetails)
                    throws Exception {

                    return s3UserService.getDirectoryContents(userDetails.getId(), path);
          }

          @PostMapping
          @ResponseStatus(HttpStatus.CREATED)
          @CreateDirectoryDocs
          public ResourceInfoResponseDTO createDirectory(@Valid
                                                         @RequestParam(name = "path")
                                                         @NotBlank(message = "Parameter \"path\" must not be blank")
                                                         String path,
                                                         @AuthenticationPrincipal
                                                         CustomUserDetails userDetails)
                    throws Exception {

                    return s3UserService.createDirectory(userDetails.getId(), path);
          }
}
