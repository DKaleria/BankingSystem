package by.grgu.identityservice.utils;

import by.grgu.identityservice.config.JwtConfig;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.*;
import javax.xml.bind.DatatypeConverter;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    private final JwtConfig jwtConfig;

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, jwtConfig.accessTokenValidity());
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, jwtConfig.refreshTokenValidity());
    }

    private String generateToken(Authentication authentication, Duration validity) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails, validity);
    }

    private String generateToken(UserDetails userDetails, Duration validity) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity.toMillis()))
                .signWith(SignatureAlgorithm.HS512, DatatypeConverter.parseBase64Binary(jwtConfig.secret()))
                .compact();
    }


    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(jwtConfig.secret()))
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().after(new Date());  // Проверяем, не истек ли срок
        } catch (ExpiredJwtException e) {
            System.out.println("Ошибка: токен истек!");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Ошибка: токен недействителен!");
            return false;
        }
    }


    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(jwtConfig.secret()))
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }


}