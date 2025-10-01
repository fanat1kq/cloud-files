package ru.example.cloudfiles.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.cloudfiles.docs.auth.LoginUserDocs;
import ru.example.cloudfiles.dto.request.UserRequestDTO;
import ru.example.cloudfiles.dto.response.UserResponseDTO;
import ru.example.cloudfiles.service.AuthService;

@RestController
@RequestMapping(value = "/api/auth")
@RequiredArgsConstructor
@Validated //TODO
public class AuthController {

          private final AuthService authService;

          private final SecurityContextLogoutHandler logoutHandler =
                    new SecurityContextLogoutHandler();


          @PostMapping("/sign-in")
          @ResponseStatus(HttpStatus.OK)
          @LoginUserDocs
          public UserResponseDTO signIn(@Valid @RequestBody UserRequestDTO request,
                                        HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse) {

                    return authService.signIn(request, httpServletRequest, httpServletResponse);
          }
}
