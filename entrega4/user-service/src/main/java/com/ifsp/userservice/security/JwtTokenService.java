package com.ifsp.userservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtTokenService {

    private final Key signingKey;

    public JwtTokenService(@Value("${jwt.secret:ChangeThisSecretForDevChangeThisSecretForDev}") String secret) {
        byte[] keyBytes = secret.getBytes();
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 1000L * 60 * 60 * 24))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Key getSigningKey() { return signingKey; }
}
