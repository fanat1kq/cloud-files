package ru.example.cloudfiles.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.example.cloudfiles.docs.auth.RegisterUserDocs;
import ru.example.cloudfiles.docs.user.GetUserDocs;
import ru.example.cloudfiles.dto.request.UserRequestDTO;
import ru.example.cloudfiles.dto.response.UserResponseDTO;
import ru.example.cloudfiles.security.CustomUserDetails;
import ru.example.cloudfiles.service.AuthService;
import ru.example.cloudfiles.service.S3UserService;


@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final AuthService authService;

    private final S3UserService s3UserService;

    @PostMapping("/auth/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    @RegisterUserDocs
    public UserResponseDTO signUp(@RequestBody UserRequestDTO request,
                                  HttpServletRequest httpServletRequest,
                                  HttpServletResponse httpServletResponse) {

        var user = authService.signUp(request, httpServletRequest, httpServletResponse);
        s3UserService.createUserDir(user.getId());

        return new UserResponseDTO(user.getUsername());
    }

    @GetMapping("/user/me")
    @ResponseStatus(HttpStatus.OK)
    @GetUserDocs
    public UserResponseDTO getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return new UserResponseDTO(userDetails.getUsername());
    }

}
