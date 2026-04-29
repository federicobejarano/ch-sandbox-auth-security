package com.helenica.auth_spike.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Per-request gatekeeper that translates a Bearer token into a populated
 * {@link org.springframework.security.core.context.SecurityContext}.
 *
 * <p><b>Why {@link OncePerRequestFilter}:</b> servlet requests can be
 * re-dispatched internally (for example forwards or error dispatches). This
 * base class guarantees the filter logic runs only once per request, avoiding
 * duplicate token parsing and duplicate writes into the security context.
 *
 * <p><b>Fast-path for public requests:</b> if the {@code Authorization}
 * header is missing or does not start with {@code Bearer }, the filter performs
 * no JWT parsing and no database lookup. It simply passes control to the rest
 * of the chain, which is exactly what we want for public endpoints such as
 * {@code /api/auth/**}.
 *
 * <p><b>Exception strategy:</b> this filter intentionally does not catch JJWT
 * parsing exceptions. If the token is malformed, expired or signed with the
 * wrong key, no authentication is written to the context and the request keeps
 * flowing as anonymous. Later, Spring Security's standard authorization layer
 * converts that into a clean {@code 401} for protected endpoints.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   AppUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
