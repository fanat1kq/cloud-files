package ru.example.cloudfiles.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.example.cloudfiles.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomUserDetails implements UserDetails {

          private static final long serialVersionUID = 42L;

          private Long id;

          private String username;

          private String password;

          private List<GrantedAuthority> authorities = new ArrayList<>();

          public CustomUserDetails() {
          }

          public CustomUserDetails(User user) {
                    this.id = user.getId();
                    this.username = user.getUsername();
                    this.password = user.getPassword();
          }

          @Override
          public Collection<? extends GrantedAuthority> getAuthorities() {
                    return authorities;
          }

          public void setAuthorities(List<GrantedAuthority> authorities) {
                    this.authorities = authorities != null ? authorities : new ArrayList<>();
          }
}