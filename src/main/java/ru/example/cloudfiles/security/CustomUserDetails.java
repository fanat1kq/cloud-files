package ru.example.cloudfiles.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.example.cloudfiles.entity.User;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@NoArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Long id;

    private String username;

    private String password;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
}