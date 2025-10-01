package ru.example.cloudfiles.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.example.cloudfiles.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

          private final UserRepository userRepository;

          @Override
          public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                    return userRepository.findUserByUsername(username)
                              .map(CustomUserDetails::new)
                              .orElseThrow(() -> new UsernameNotFoundException(
                                        "User not found: " + username));
          }
}