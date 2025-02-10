package org.jordijaspers.eventify.common.config;

import java.util.Arrays;
import java.util.List;

import org.jordijaspers.eventify.api.authentication.model.Permission;
import org.jordijaspers.eventify.common.security.converter.JwtAuthenticationConverter;
import org.jordijaspers.eventify.common.security.filter.ApiKeyAuthenticationFilter;
import org.jordijaspers.eventify.common.security.filter.JwtAuthenticationFilter;
import org.jordijaspers.eventify.common.security.filter.UnauthorizedHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.jordijaspers.eventify.api.Paths.*;
import static org.jordijaspers.eventify.api.token.model.JWTClaimNames.PERMISSIONS;
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
public class WebSecurityConfig implements WebMvcConfigurer {

    /**
     * Configure CORS & requests handling behaviour.
     **/
    @Bean
    public SecurityFilterChain filterChain(@Value("${security.cors.allowed-origins}") final String[] allowedOrigins,
        final JwtAuthenticationFilter jwtAuthenticationFilter,
        final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
        final JwtAuthenticationConverter jwtAuthenticationConverter,
        final UnauthorizedHandler unauthorizedHandler,
        final HttpSecurity http) throws Exception {

        // Adding a once per request filter to check the JWT token.
        http.addFilterAfter(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(jwtAuthenticationFilter, ApiKeyAuthenticationFilter.class);

        // Configure Session Management.
        http.sessionManagement(sessionConfiguration -> sessionConfiguration.sessionCreationPolicy(STATELESS));

        // Return 401 (unauthorized) instead of 302 (redirect to login) when an authorization is missing or invalid.
        http.exceptionHandling(handler -> {
            handler.authenticationEntryPoint(unauthorizedHandler);
        });

        // Disable CSRF because of state-less session-management.
        http.csrf(AbstractHttpConfigurer::disable);

        // Enable CORS.
        http.cors(cors -> cors.configurationSource(corsConfigurationSource(allowedOrigins)));

        // Configure OAuth2 Resource Server.
        http.oauth2ResourceServer(oAuth2Configurer -> {
            oAuth2Configurer.jwt(jwtConfigurer -> {
                final JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
                converter.setAuthorityPrefix("");
                converter.setAuthoritiesClaimName(PERMISSIONS);
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(converter);
                jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter);
            });
        });

        // Configure Endpoints
        http.authorizeHttpRequests(accessManagement -> {
            accessManagement.requestMatchers(PUBLIC_ACTUATOR_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(PUBLIC_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(AUTH_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(OPENAPI_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(ERROR_PATH).permitAll();

            accessManagement.requestMatchers(EXTERNAL_BASE_PATH + WILDCARD_PART).hasAnyRole(Permission.ACCESS_EXTERNAL.name());

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
        configuration.setAllowCredentials(true);

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(WILDCARD_PART, configuration);
        return source;
    }
}
