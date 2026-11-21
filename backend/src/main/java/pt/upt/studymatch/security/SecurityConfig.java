package pt.upt.studymatch.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Who may call what. Every rule is here, by URL, so new endpoints are protected without
 * touching their controllers:
 *
 * <ul>
 *   <li>Only the health check and the login are public.</li>
 *   <li>Accounts, degrees, course units and changes to students need an ADMIN.</li>
 *   <li>Everything else in the API needs a TEACHER. An ADMIN can do everything a TEACHER can.</li>
 * </ul>
 *
 * <p>Logins return a signed token (JWT) that the frontend sends as {@code Authorization: Bearer}.
 * There are no sessions or cookies, so CSRF protection is not needed.
 */
@Configuration
public class SecurityConfig {

    private static final int MIN_SECRET_BYTES = 32;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter tokenConverter,
            JsonSecurityErrors errors) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/accounts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/degrees", "/api/course-units", "/api/students")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/degrees/*", "/api/course-units/*", "/api/students/*")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/degrees/*", "/api/course-units/*", "/api/students/*")
                                .hasRole("ADMIN")
                        .requestMatchers("/api/**").hasRole("TEACHER")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(server -> server
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(tokenConverter))
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors));
        return http.build();
    }

    @Bean
    RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_TEACHER");
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecretKey tokenSigningKey(@Value("${studymatch.security.jwt-secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("studymatch.security.jwt-secret (JWT_SECRET) must have at least "
                    + MIN_SECRET_BYTES + " characters");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey tokenSigningKey) {
        return NimbusJwtEncoder.withSecretKey(tokenSigningKey).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey tokenSigningKey) {
        return NimbusJwtDecoder.withSecretKey(tokenSigningKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /** Reads the account's role from the token's {@code roles} claim, e.g. ADMIN becomes ROLE_ADMIN. */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(TokenService.ROLES_CLAIM);
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }
}
