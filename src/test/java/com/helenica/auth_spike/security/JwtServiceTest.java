package com.helenica.auth_spike.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String PRIMARY_SECRET = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes());

    private static final String SECONDARY_SECRET = Base64.getEncoder()
            .encodeToString("fedcba9876543210fedcba9876543210".getBytes());

    private static final long ONE_HOUR_IN_MS = 3_600_000L;

    @Test
    void generateTokenIncludesExpectedStandardAndCustomClaims() {
        JwtService jwtService = new JwtService(PRIMARY_SECRET, ONE_HOUR_IN_MS);
        UserDetails userDetails = userDetails("alice@example.com", "ADMIN");

        String token = jwtService.generateToken(userDetails);
        Claims claims = parseClaims(token, PRIMARY_SECRET);

        assertEquals("alice@example.com", claims.getSubject());
        assertEquals("ROLE_ADMIN", claims.get("role", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void extractUsernameReturnsSubjectFromValidToken() {
        JwtService jwtService = new JwtService(PRIMARY_SECRET, ONE_HOUR_IN_MS);
        UserDetails userDetails = userDetails("bob@example.com", "USER");

        String token = jwtService.generateToken(userDetails);

        assertEquals("bob@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void extractUsernameThrowsWhenTokenIsExpired() {
        JwtService jwtService = new JwtService(PRIMARY_SECRET, -1_000L);
        UserDetails userDetails = userDetails("expired@example.com", "USER");

        String token = jwtService.generateToken(userDetails);

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void extractUsernameThrowsWhenTokenSignatureDoesNotMatch() {
        JwtService issuingService = new JwtService(PRIMARY_SECRET, ONE_HOUR_IN_MS);
        JwtService validatingService = new JwtService(SECONDARY_SECRET, ONE_HOUR_IN_MS);
        UserDetails userDetails = userDetails("mallory@example.com", "USER");

        String token = issuingService.generateToken(userDetails);

        assertThrows(SignatureException.class, () -> validatingService.extractUsername(token));
    }

    private static UserDetails userDetails(String email, String role) {
        return User.withUsername(email)
                .password("unused")
                .roles(role)
                .build();
    }

    private static Claims parseClaims(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
