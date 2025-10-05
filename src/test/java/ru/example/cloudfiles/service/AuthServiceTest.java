package ru.example.cloudfiles.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import ru.example.cloudfiles.dto.request.UserRequestDTO;
import ru.example.cloudfiles.dto.response.UserResponseDTO;
import ru.example.cloudfiles.entity.User;
import ru.example.cloudfiles.mapper.UserMapper;
import ru.example.cloudfiles.repository.UserRepository;
import ru.example.cloudfiles.security.CustomUserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final SecurityContextRepository securityContextRepository = mock(SecurityContextRepository.class);
    private final UserMapper userMapper = mock(UserMapper.class);

    private final AuthService authService = new AuthService(
            userRepository, passwordEncoder, userDetailsService, securityContextRepository, userMapper
    );

    @Test
    @DisplayName("signUp encodes password, saves user, authenticates, and returns saved user")
    void signUp_ok() {
        UserRequestDTO req = new UserRequestDTO("johnny", "password123");
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        User toSave = new User();
        toSave.setUsername("johnny");
        toSave.setPassword("encoded");

        User saved = new User();
        saved.setId(10L);
        saved.setUsername("johnny");
        saved.setPassword("encoded");

        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userMapper.toEntity(any(UserRequestDTO.class))).thenReturn(new User());
        when(userMapper.toEntityWithPassword(eq(req), eq(passwordEncoder))).thenCallRealMethod();

        when(userMapper.toEntity(req)).thenReturn(new User());
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(userMapper.toCustomUserDetails(saved)).thenReturn(customUser(10L, "johnny", "encoded"));

        User result = authService.signUp(req, httpReq, httpRes);

        assertEquals(10L, result.getId());
        assertEquals("johnny", result.getUsername());

        ArgumentCaptor<SecurityContext> ctxCaptor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(securityContextRepository).saveContext(ctxCaptor.capture(), eq(httpReq), eq(httpRes));
        assertNotNull(ctxCaptor.getValue());
        assertNotNull(ctxCaptor.getValue().getAuthentication());
        assertEquals("johnny", ((CustomUserDetails) ctxCaptor.getValue().getAuthentication().getPrincipal()).getUsername());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("signIn validates password, authenticates, and returns DTO")
    void signIn_ok() {
        UserRequestDTO req = new UserRequestDTO("mike", "secret");
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        CustomUserDetails details = customUser(5L, "mike", "hash");
        when(userDetailsService.loadUserByUsername("mike")).thenReturn(details);
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        UserResponseDTO dto = new UserResponseDTO("mike");
        when(userMapper.toDto(details)).thenReturn(dto);

        UserResponseDTO result = authService.signIn(req, httpReq, httpRes);

        assertEquals("mike", result.username());
        verify(securityContextRepository).saveContext(any(SecurityContextImpl.class), eq(httpReq), eq(httpRes));
    }

    @Test
    @DisplayName("signIn with wrong password throws BadCredentialsException")
    void signIn_badPassword() {
        UserRequestDTO req = new UserRequestDTO("mike", "bad");
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        CustomUserDetails details = customUser(5L, "mike", "hash");
        when(userDetailsService.loadUserByUsername("mike")).thenReturn(details);
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.signIn(req, httpReq, httpRes));
        verify(securityContextRepository, never()).saveContext(any(), any(), any());
    }

    private CustomUserDetails customUser(long id, String username, String password) {
        CustomUserDetails user = new CustomUserDetails();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }
}
