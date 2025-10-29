package com.acme.javamigrator.server.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    // In a real system, inject via configuration/secret management
    private final SecretKey key = Keys.hmacShaKeyFor("replace-with-strong-secret-key-32-bytes-minimum".getBytes());

    public String generateToken(String username, long expiresInSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expiresInSeconds)))
                .addClaims(Map.of("username", username))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
