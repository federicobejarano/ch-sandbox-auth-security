package com.helenica.auth_spike.config;

import com.helenica.auth_spike.security.AppUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Central security configuration for the spike.
 *
 * <p><b>Why three annotations:</b>
 * <ul>
 *   <li>{@code @Configuration} -- declares the class as a source of beans.
 *       Required explicitly because, in Spring Security 6+,
 *       {@code @EnableWebSecurity} no longer carries
 *       {@code @Configuration} as a meta-annotation (a breaking change vs
 *       Security 5).</li>
 *   <li>{@code @EnableWebSecurity} -- imports
 *       {@code WebSecurityConfiguration} and bootstraps the
 *       {@code FilterChainProxy} that drives the servlet filter chain.</li>
 *   <li>{@code @EnableMethodSecurity} -- activates
 *       {@code @PreAuthorize}/{@code @PostAuthorize}. Without it those
 *       annotations are silently ignored, which is one of the most
 *       confusing failure modes for newcomers.</li>
 * </ul>
 *
 * <p><b>JWT filter wiring:</b> the {@code JwtAuthenticationFilter} that
 * actually inspects the {@code Authorization} header is introduced in
 * Activity 8 and registered there via
 * {@code .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)}.
 * Until that activity lands, this configuration only contains the static
 * pieces (URL matrix, providers, encoder, CORS) so the codebase keeps
 * compiling.
 *
 * <p><b>Spring Security 7 API note:</b>
 * {@link DaoAuthenticationProvider} no longer exposes a no-arg constructor
 * + {@code setUserDetailsService(...)}. The {@link AppUserDetailsService}
 * must now be supplied through the constructor, which makes the dependency
 * explicit and the provider effectively immutable after construction.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserDetailsService appUserDetailsService;

    public SecurityConfig(AppUserDetailsService appUserDetailsService) {
        this.appUserDetailsService = appUserDetailsService;
    }

    /**
     * Defines the single {@link SecurityFilterChain} that governs the
     * entire HTTP API. Every DSL call below maps 1:1 to a row of the
     * design document's Section 2.3 breakdown.
     *
     * <p>The {@code authorizeHttpRequests} matrix is order-sensitive:
     * matchers are evaluated top to bottom and the first match wins.
     * {@code permitAll()} entries therefore precede the catch-all
     * {@code anyRequest().authenticated()}.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())

                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin()))

                .authenticationProvider(authenticationProvider())

                .cors(Customizer.withDefaults());

        return http.build();
    }

    /**
     * The {@link DaoAuthenticationProvider} is the workhorse that powers
     * {@code authenticationManager.authenticate(...)} during
     * {@code POST /api/auth/login}. It composes two collaborators:
     * {@link AppUserDetailsService} (loads the principal from H2) and
     * {@link #passwordEncoder()} (BCrypt-compares the submitted
     * password against the stored hash).
     *
     * <p>If the lookup fails or the hash does not match, the provider
     * throws {@code BadCredentialsException}, which the controller layer
     * will eventually translate to a {@code 401}.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(appUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the {@link AuthenticationManager} that the upcoming
     * {@code AuthService.login(...)} (Activity 9) will inject to drive
     * the authentication call. Pulling it from
     * {@link AuthenticationConfiguration} is the canonical recipe in
     * Spring Security 6+, replacing the old
     * {@code WebSecurityConfigurerAdapter#authenticationManagerBean()}.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt with the default strength (10). The same singleton bean is
     * consumed by:
     * <ul>
     *   <li>{@link DaoAuthenticationProvider} -- to verify login passwords.</li>
     *   <li>{@code DevDataSeederConfig} -- to hash the seed users'
     *       passwords. Sharing the bean guarantees that the algorithm
     *       used to hash on seed and the algorithm used to verify on
     *       login can never drift apart.</li>
     * </ul>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the CORS policy that {@code .cors(Customizer.withDefaults())}
     * picks up by bean name.
     *
     * <p><b>Why a {@link CorsConfigurationSource} bean and not
     * {@code WebMvcConfigurer.addCorsMappings()}:</b> when Spring Security
     * is active the {@code CorsFilter} sits inside the
     * {@code SecurityFilterChain} and runs <em>before</em> the MVC
     * dispatcher. MVC-level CORS is therefore bypassed: preflight
     * {@code OPTIONS} requests would be rejected by the security chain
     * before MVC ever sees them. Anchoring CORS on a bean here ensures
     * preflight is answered at the right point in the chain.
     *
     * <p><b>{@code allowCredentials = false}:</b> we transport the JWT
     * via the {@code Authorization} header, never via cookies. With
     * credentials disabled, the browser will not include cookies on
     * cross-origin requests and the response cannot use the
     * {@code Access-Control-Allow-Origin: *} wildcard pitfall.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
