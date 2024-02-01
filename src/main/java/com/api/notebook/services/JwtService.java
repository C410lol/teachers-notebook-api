package com.api.notebook.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final UserService userService;

    @Value("${jwt.secret.key}")
    private String secretKey;

    private @NotNull Key getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String email) { //Generate a token
        return "Bearer " + Jwts.builder()
                .setSubject(email)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) { //Extract all claims from token
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //Extract one claim from token
    private <T> T extractClaim(String token, @NotNull Function<Claims, T> claimsTFunction) {
        return claimsTFunction.apply(extractAllClaims(token));
    }

    public String getEmailByToken(String token) { //Get email by token
        return extractClaim(token, Claims::getSubject);
    }

    public Authentication tryToAuthenticate(String token) { //Try to authenticate user by token
        var teacherOptional = userService.findUserByEmail(getEmailByToken(token));
        if (teacherOptional.isPresent()) { //Verify if the user exists
            if (teacherOptional.get().isVerified()) {
                return new UsernamePasswordAuthenticationToken( //If user exists returns an authentication
                        teacherOptional.get().getId(),
                        teacherOptional.get().getPassword(),
                        List.of(new SimpleGrantedAuthority(teacherOptional.get().getRole().name()))
                );
            }
            return new UsernamePasswordAuthenticationToken(
                    teacherOptional.get().getId(),
                    teacherOptional.get().getPassword(),
                    Collections.emptyList()
            );
        }
        return null;
    }

}
