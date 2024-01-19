package com.moneta.hub.moneta.security;

import com.moneta.hub.moneta.model.entity.MonetaUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtGenerator {

    /**
     * Expiration time in milliseconds (15 minutes)
     */
    private static final long JWT_EXPIRATION = 900000L;

    /**
     * Key used for signing JWT token
     */
    @Value("${jwt.key}")
    private String jwtKey;

    @Value("${spring.application.name}")
    private String applicationName;

    public String generateToken(Authentication authentication, MonetaUser user) {
        String username = authentication.getName();
        Date issueDate = new Date();
        Date expireDate = new Date(issueDate.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                   .issuer(applicationName)
                   .subject(username)
                   .claim("roles", user.getRoles().stream().map(roles -> roles.getName().name()).toList())
                   .issuedAt(issueDate)
                   .expiration(expireDate)
                   .signWith(getSigningKey())
                   .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                            .verifyWith(getSigningKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        Claims claims = Jwts.parser()
                            .verifyWith(getSigningKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        try {
            return notExpired(claims.getIssuedAt(), claims.getExpiration());
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT is expired or invalid.");
        }
    }

    private boolean notExpired(Date issuedAt, Date expireAt) {
        return issuedAt.before(expireAt);
    }
}
