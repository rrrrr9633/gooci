package com.gooci.console.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @ClassName TokenSupport
 * @Description JWT 签发与解析
 * @author GooCi
 * @date 2026-03-23
 */
@Component
public class TokenSupport {

    private final SecretKey secretKey;
    private final long expireHours;

    public TokenSupport(
            @Value("${gooci.jwt-secret}") String jwtSecret,
            @Value("${gooci.jwt-expire-hours}") long expireHours) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expireHours = expireHours;
    }

    public String issue(String userId) {
        return issue(userId, "enter");
    }

    public String issue(String userId, String scope) {
        Instant now = Instant.now();
        String tokenScope = scope == null || scope.isBlank() ? "enter" : scope.trim();
        return Jwts.builder()
                .subject(userId)
                .claim("scope", tokenScope)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expireHours, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }

    public String parseUserId(String token) {
        return claims(token).getSubject();
    }

    public String parseScope(String token) {
        String scope = claims(token).get("scope", String.class);
        return scope == null || scope.isBlank() ? "enter" : scope;
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
