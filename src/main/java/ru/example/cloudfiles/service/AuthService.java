package ru.example.cloudfiles.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.cloudfiles.dto.request.UserRequestDTO;
import ru.example.cloudfiles.dto.response.UserResponseDTO;
import ru.example.cloudfiles.entity.User;
import ru.example.cloudfiles.mapper.UserMapper;
import ru.example.cloudfiles.repository.UserRepository;
import ru.example.cloudfiles.security.CustomUserDetails;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository;
    private final UserMapper userMapper;

    public User signUp(UserRequestDTO request,
                       HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse) {

        User user = userMapper.toEntityWithPassword(request, passwordEncoder);
        User savedUser = userRepository.save(user);

        authenticateUser(savedUser, httpServletRequest, httpServletResponse);
        return savedUser;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO signIn(UserRequestDTO request,
                                  HttpServletRequest httpServletRequest,
                                  HttpServletResponse httpServletResponse) {

        CustomUserDetails userDetails = (CustomUserDetails)
                userDetailsService.loadUserByUsername(request.username());

        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password for user: " + request.username());
        }

        authenticateUser(userDetails, httpServletRequest, httpServletResponse);
        return userMapper.toDto(userDetails);
    }

    public void authenticateUser(User user,
                                 HttpServletRequest httpRequest,
                                 HttpServletResponse httpResponse) {
        CustomUserDetails userDetails = userMapper.toCustomUserDetails(user);
        authenticateUser(userDetails, httpRequest, httpResponse);
    }

    private void authenticateUser(CustomUserDetails userDetails,
                                  HttpServletRequest httpRequest,
                                  HttpServletResponse httpResponse) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = new SecurityContextImpl(authentication);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }
}