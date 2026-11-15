package com.technicel.security;

import com.technicel.config.AppProperties;
import com.technicel.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final AppProperties appProperties;
    private final SecretKey key;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.key = Keys.hmacShaKeyFor(signingKeyBytes(appProperties.getJwt().getSecret()));
    }

    public String generateToken(Long userId, String nip, Role role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(appProperties.getJwt().getExpirationMs());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("nip", nip)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public UserPrincipal parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            String nip = claims.get("nip", String.class);
            Role role = Role.valueOf(claims.get("role", String.class));
            return new UserPrincipal(userId, nip, role, true);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidJwtException("Invalid or expired token");
        }
    }

    private byte[] signingKeyBytes(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) {
            return bytes;
        }
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required to derive the JWT key", ex);
        }
    }
}
