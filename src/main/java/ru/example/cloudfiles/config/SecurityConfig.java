package ru.example.cloudfiles.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

          @Bean
          public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                    http
                              .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                              .csrf(AbstractHttpConfigurer::disable)
                              .securityContext(securityContext -> securityContext
                                        .securityContextRepository(securityContextRepository())
                              )
                              .sessionManagement(
                                        sessionManagement -> sessionManagement
                                                  .maximumSessions(1)
                                                  .expiredUrl("/api/auth/sign-in")
                              )
                              .authorizeHttpRequests(authorize -> authorize
                                        .requestMatchers("/api/auth/sign-up",
                                                  "/api/auth/sign-in",
                                                  "/actuator/health").permitAll()
                                        .requestMatchers("/v3/api-docs/**",
                                                  "/swagger-ui/**",
                                                  "/swagger-ui.html").permitAll()
                                        .anyRequest().authenticated())
                              .formLogin(AbstractHttpConfigurer::disable)
                              .logout(logout -> logout
                                        .logoutUrl("/api/auth/sign-out")
                                        .deleteCookies("SESSION", "JSESSIONID")
                                        .invalidateHttpSession(true)
                                        .clearAuthentication(true)
                                        .logoutSuccessHandler(logoutSuccessHandler()))
                              .exceptionHandling(configurer -> configurer.authenticationEntryPoint(
                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
                    return http.build();
          }

          @Bean
          public LogoutSuccessHandler logoutSuccessHandler() {

                    return (request, response, authentication) -> response.setStatus(HttpStatus.NO_CONTENT.value());
          }

          @Bean
          @ConditionalOnMissingBean
          public HttpSessionEventPublisher httpSessionEventPublisher() {
                    return new HttpSessionEventPublisher();
          }

          @Bean
          public CorsConfigurationSource corsConfigurationSource() {
                    return request -> {
                              CorsConfiguration config = new CorsConfiguration();
                              config.setAllowedOriginPatterns(List.of("*"));
                              config.setAllowedMethods(
                                        List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                              config.setAllowedHeaders(List.of("*"));
                              config.setAllowCredentials(true);
                              return config;
                    };
          }

          @Bean
          public PasswordEncoder passwordEncoder() {
                    return new BCryptPasswordEncoder();
          }

          @Bean
          public SecurityContextRepository securityContextRepository() {
                    return new HttpSessionSecurityContextRepository();
          }
}