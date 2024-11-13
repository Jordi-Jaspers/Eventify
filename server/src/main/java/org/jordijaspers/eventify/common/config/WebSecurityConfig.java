package org.jordijaspers.eventify.common.config;

import org.jordijaspers.eventify.common.security.converter.JwtAuthenticationConverter;
import org.jordijaspers.eventify.common.security.entrypoint.DefaultAuthenticationEntryPoint;
import org.jordijaspers.eventify.common.security.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.jordijaspers.eventify.api.Paths.*;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Configures spring web security to have Keycloak as the SSO authentication provider.
 * <p>
 * {@inheritDoc}
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true
)
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class WebSecurityConfig {

    /**
     * Configure CORS & requests handling behaviour.
     **/
    @Bean
    public SecurityFilterChain filterChain(@Value("${security.cors.allowed-origins}") final String[] allowedOrigins,
        final DefaultAuthenticationEntryPoint authenticationEntryPoint,
        final JwtAuthenticationFilter jwtAuthenticationFilter,
        final JwtAuthenticationConverter jwtAuthenticationConverter,
        final AuthenticationProvider authenticationProvider,
        final HttpSecurity http) throws Exception {

        // Adding a once per request filter to check the JWT token.
        http.addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Adding a custom authentication provider.
        http.authenticationProvider(authenticationProvider);

        // Disable CSRF because of state-less session-management.
        http.csrf(AbstractHttpConfigurer::disable);

        // Enable CORS.
        http.cors(cors -> cors.configurationSource(corsConfigurationSource(allowedOrigins)));

        // Configure Session Management.
        http.sessionManagement(sessionConfiguration -> sessionConfiguration.sessionCreationPolicy(STATELESS));

        // Configure OAuth2 Resource Server.
        http.oauth2ResourceServer(oAuth2Configurer -> {
            oAuth2Configurer.jwt(jwtConfigurer -> {
                final JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
                converter.setAuthorityPrefix("");
                converter.setAuthoritiesClaimName("roles");
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(converter);
                jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter);
            });
        });

        // Add Exception Handling for unauthorized requests.
        http.exceptionHandling(handler -> handler.authenticationEntryPoint(authenticationEntryPoint));

        // Configure Endpoints
        http.authorizeHttpRequests(accessManagement -> {
            accessManagement.requestMatchers(PUBLIC_ACTUATOR_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(PUBLIC_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(AUTH_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(OPENAPI_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(ERROR_PATH).permitAll();

            accessManagement.requestMatchers(LOGOUT_PATH).authenticated();
            accessManagement.anyRequest().authenticated();
        });

        return http.build();
    }

    private UrlBasedCorsConfigurationSource corsConfigurationSource(final String... origins) {
        final var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(origins));
        configuration.setAllowedMethods(List.of(WILDCARD));
        configuration.setAllowedHeaders(List.of(WILDCARD));
        configuration.setExposedHeaders(List.of(WILDCARD));

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(WILDCARD_PART, configuration);
        return source;
    }
}
