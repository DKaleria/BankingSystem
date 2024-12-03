package by.grgu.identityservice.utils;

import by.grgu.identityservice.exceptions.ErrorMessage;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;

import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Collectors;
import javax.xml.bind.DatatypeConverter;

@Component
public class JwtTokenUtil {
    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 hours
    private final String secret = "T42d4f6a685f536563cD7572655f4b657fff9";

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) .peek(role -> System.out.println("Role: " + role)) // Логирование для проверки
                .collect(Collectors.toList());

        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, DatatypeConverter.parseBase64Binary(secret))
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secret)).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(secret))
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    public Object getRolesFromToken(String token) {
        try {
            Object rolesObj = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(secret))
                    .parseClaimsJws(token)
                    .getBody()
                    .get("roles");

            if (rolesObj instanceof List<?>) {
                List<String> roles = new ArrayList<>();
                for (Object role : (List<?>) rolesObj) {
                    if (role instanceof String) {
                        roles.add((String) role);
                    }
                }
                return roles;
            } else {
                return new ErrorMessage(new Date(), "Роли не найдены");
            }
        } catch (JwtException e) {
            return new ErrorMessage(new Date(), "Ошибка валидации токена: " + e.getMessage());
        }
    }
}