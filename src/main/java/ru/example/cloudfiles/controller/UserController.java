package ru.example.cloudfiles.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.cloudfiles.docs.auth.RegisterUserDocs;
import ru.example.cloudfiles.docs.user.GetUserDocs;
import ru.example.cloudfiles.dto.request.UserRequestDTO;
import ru.example.cloudfiles.dto.response.UserResponseDTO;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.S3Service;
import ru.example.cloudfiles.service.UserService;


@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    private final S3Service s3Service;

    @PostMapping("/auth/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    @RegisterUserDocs
    public UserResponseDTO signUp(@RequestBody UserRequestDTO request,
                                  HttpServletRequest httpServletRequest,
                                  HttpServletResponse httpServletResponse) {

        var user = userService.signUp(request, httpServletRequest, httpServletResponse);
        s3Service.createUserDirection(user.getId());

        return new UserResponseDTO(user.getUsername());
    }

    @GetMapping("/user/me")
    @ResponseStatus(HttpStatus.OK)
    @GetUserDocs
    public UserResponseDTO getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return new UserResponseDTO(userDetails.getUsername());
    }

}
