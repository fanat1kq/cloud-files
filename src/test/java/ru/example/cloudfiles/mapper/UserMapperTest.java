package ru.example.cloudfiles.mapper;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.example.cloudfiles.dto.request.UserRequestDTO;
import ru.example.cloudfiles.dto.response.UserResponseDTO;
import ru.example.cloudfiles.entity.User;
import ru.example.cloudfiles.security.CustomUserDetails;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class UserMapperTest {

    @InjectMocks
    private UserMapperImpl userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PodamFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PodamFactoryImpl();
    }

    @Test
    @DisplayName("Should map CustomUserDetails to UserResponseDTO")
    void toDtoFromCustomUserDetails(SoftAssertions softly) {

        CustomUserDetails userDetails = factory.manufacturePojo(CustomUserDetails.class);

        UserResponseDTO result = userMapper.toDto(userDetails);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.username()).isEqualTo(userDetails.getUsername());
    }

    @Test
    @DisplayName("Should map UserRequestDTO to User without password")
    void toEntityFromUserRequestDTO(SoftAssertions softly) {

        UserRequestDTO dto = factory.manufacturePojo(UserRequestDTO.class);

        User result = userMapper.toEntity(dto);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.getUsername()).isEqualTo(dto.username());
        softly.assertThat(result.getId()).isNull();
        softly.assertThat(result.getPassword()).isNull();
    }

    @Test
    @DisplayName("Should map User to CustomUserDetails")
    void toCustomUserDetailsFromUser(SoftAssertions softly) {

        User user = factory.manufacturePojo(User.class);

        CustomUserDetails result = userMapper.toCustomUserDetails(user);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.getUsername()).isEqualTo(user.getUsername());
        softly.assertThat(result.getId()).isEqualTo(user.getId());
        softly.assertThat(result.getPassword()).isEqualTo(user.getPassword());
        softly.assertThat(result.getAuthorities()).isNotNull();
    }

    @Test
    @DisplayName("Should map UserRequestDTO to User with encoded password")
    void toEntityWithPassword(SoftAssertions softly) {

        UserRequestDTO dto = factory.manufacturePojo(UserRequestDTO.class);
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.encode(dto.password())).thenReturn(encodedPassword);

        User result = userMapper.toEntityWithPassword(dto, passwordEncoder);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.getUsername()).isEqualTo(dto.username());
        softly.assertThat(result.getPassword()).isEqualTo(encodedPassword);

        verify(passwordEncoder).encode(dto.password());
    }
}