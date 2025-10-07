package ru.example.cloudfiles.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("signUp encodes password, saves user, authenticates, and returns saved user")
    void signUpOk() {

        UserRequestDTO req = factory.manufacturePojo(UserRequestDTO.class);
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        User saved = factory.manufacturePojo(User.class);

        when(passwordEncoder.encode(req.password())).thenReturn("encoded");
        when(userMapper.toEntity(any(UserRequestDTO.class))).thenReturn(new User());
        when(userMapper.toEntityWithPassword(req, passwordEncoder)).thenCallRealMethod();

        when(userMapper.toEntity(req)).thenReturn(new User());
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(userMapper.toCustomUserDetails(saved)).thenReturn(customUser(saved.getId(), saved.getUsername(), "encoded"));

        User result = authService.signUp(req, httpReq, httpRes);

        assertEquals(saved.getId(), result.getId());
        assertEquals(saved.getUsername(), result.getUsername());

        ArgumentCaptor<SecurityContext> ctxCaptor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(securityContextRepository).saveContext(ctxCaptor.capture(), eq(httpReq), eq(httpRes));
        assertNotNull(ctxCaptor.getValue());
        assertNotNull(ctxCaptor.getValue().getAuthentication());
        assertEquals(saved.getUsername(), ((CustomUserDetails) ctxCaptor.getValue().getAuthentication().getPrincipal()).getUsername());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("signIn validates password, authenticates, and returns DTO")
    void signInOk() {

        UserRequestDTO req = factory.manufacturePojo(UserRequestDTO.class);
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        CustomUserDetails details = factory.manufacturePojo(CustomUserDetails.class);
        when(userDetailsService.loadUserByUsername(req.username())).thenReturn(details);
        when(passwordEncoder.matches(req.password(), details.getPassword())).thenReturn(true);

        UserResponseDTO dto = factory.manufacturePojo(UserResponseDTO.class);
        when(userMapper.toDto(details)).thenReturn(dto);

        UserResponseDTO result = authService.signIn(req, httpReq, httpRes);

        assertEquals(dto.username(), result.username());
        verify(securityContextRepository).saveContext(any(SecurityContextImpl.class), eq(httpReq), eq(httpRes));
    }

    @Test
    @DisplayName("signIn with wrong password throws BadCredentialsException")
    void signInBadPassword() {

        UserRequestDTO req = factory.manufacturePojo(UserRequestDTO.class);
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        CustomUserDetails details = factory.manufacturePojo(CustomUserDetails.class);
        when(userDetailsService.loadUserByUsername(req.username())).thenReturn(details);
        when(passwordEncoder.matches(req.password(), details.getPassword())).thenReturn(false);

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