package com.api.notebook.configs.security;

import com.api.notebook.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        var authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION); //Get the authorization header
        if (authorizationHeader != null &&
                !authorizationHeader.isBlank() &&
                authorizationHeader.startsWith("Bearer ")) { //some verifications about the header

            //Try to authenticate
            var authentication = jwtService.tryToAuthenticate(authorizationHeader.substring(7));
            SecurityContextHolder.getContext().setAuthentication(authentication); //Update the SecurityContextHolder
        }
        filterChain.doFilter(request, response);
    }

}
