package com.synapse.platform.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenTtlMs;

    public JwtTokenProvider(
        @Value("${synapse.jwt.secret}") String secret,
        @Value("${synapse.jwt.access-ttl-ms:3600000}") long accessTokenTtlMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlMs = accessTokenTtlMs;
    }

    public String issueAccessToken(String subject) {
        Date now = new Date();
        return Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + accessTokenTtlMs))
            .signWith(secretKey)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
