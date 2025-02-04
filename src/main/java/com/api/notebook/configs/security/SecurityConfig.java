package com.api.notebook.configs.security;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.
                cors(Customizer.withDefaults()) //Using custom configurations for CORS
                .csrf(AbstractHttpConfigurer::disable) //Disable CSRF
                .authorizeHttpRequests(auth -> //Authorizing some endpoints
                        auth.requestMatchers(
                                        "/users/login",
                                        "/users/get-by-email**",
                                        "/users/{userId}/change-password",
                                        "/users/{teacherId}/verified",
                                        "/users/check-auth",
                                        "/teachers/create**",
                                        "/admins/create**",
                                        "/institutions/all-by-name**")
                                .permitAll().anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) //Adding JWT filter
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() { //Custom configurations for CORS
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*")); //Allowing all origins
        corsConfiguration.setAllowedHeaders(List.of("*")); //Allowing headers
        corsConfiguration.setAllowedMethods(List.of("POST", "GET", "PUT", "DELETE")); //Allowing methods
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return urlBasedCorsConfigurationSource;
    }

}
