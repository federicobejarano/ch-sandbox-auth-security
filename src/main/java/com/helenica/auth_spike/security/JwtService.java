package com.helenica.auth_spike.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Encapsulates every interaction with the JJWT library so that the rest of
 * the application depends on a thin, intention-revealing API ({@code generate},
 * {@code extract}, {@code isValid}) instead of the raw builder/parser DSL.
 *
 * <p><b>Why a dedicated service:</b> isolating JJWT here honours the Single
 * Responsibility Principle and makes the token logic trivially unit-testable
 * (see the learning tests planned for Activity 14). If we ever swap JJWT for
 * Nimbus or rotate to asymmetric keys, only this class changes.
 *
 * <p><b>Key lifecycle:</b> the {@link SecretKey} is materialised <em>once</em>
 * in the constructor and stored as a {@code final} field. Decoding the Base64
 * secret and building the {@code HmacSHA256} key is not free, and Spring
 * guarantees a singleton bean scope, so amortising this work across the bean's
 * lifetime is both correct and efficient.
 *
 * <p><b>Algorithm inference:</b> {@code .signWith(signingKey)} alone is enough
 * to make JJWT pick {@code HS256} because the {@link SecretKey} carries its
 * algorithm identifier. Passing the algorithm explicitly would be redundant
 * and is actually deprecated in JJWT 0.12.x.
 *
 * <p><b>Exception strategy:</b> JJWT raises a family of unchecked exceptions
 * ({@code ExpiredJwtException}, {@code SignatureException},
 * {@code MalformedJwtException}, ...). This service deliberately does
 * <em>not</em> catch them. They bubble up to {@code JwtAuthenticationFilter},
 * which simply skips setting the {@code SecurityContext} and lets the standard
 * {@code AuthenticationEntryPoint} answer with a clean {@code 401}. Swallowing
 * them here would leak invalid tokens further into the chain.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    /**
     * Builds a compact JWS with the user's email as the {@code sub} claim and
     * the first {@link org.springframework.security.core.GrantedAuthority} as
     * a custom {@code role} claim.
     *
     * <p>Only the first authority is propagated because in this spike
     * {@link UserDetails} carries exactly one role (cf.
     * {@code AppUserDetailsService} contract from Section 2.6 of the design).
     * If multi-role support is ever introduced, this method should serialise
     * the full collection (e.g. as a JSON array claim).
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().iterator().next()
                        .getAuthority())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Returns the {@code sub} claim, which by convention carries the user's
     * email. Any signature/format/expiration error surfaces as a JJWT runtime
     * exception (see the class-level Javadoc on the exception strategy).
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Two-fold check: the token must belong to the same principal we just
     * loaded from the database <em>and</em> it must not be expired. Both
     * conditions are required: a token can carry a syntactically valid
     * subject and still be expired (or vice versa, an expired token whose
     * subject still matches a live account).
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Verifies the signature with the same symmetric key used to sign and
     * returns the {@link Claims} payload. {@code parseSignedClaims()} fails
     * fast with a typed exception when the JWS is tampered with, expired or
     * malformed -- callers rely on this to discard untrusted tokens without
     * an explicit {@code try/catch} in this class.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
