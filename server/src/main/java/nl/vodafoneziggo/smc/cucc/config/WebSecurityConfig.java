package nl.vodafoneziggo.smc.cucc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static nl.vodafoneziggo.smc.cucc.api.Paths.WILDCARD;
import static nl.vodafoneziggo.smc.cucc.api.Paths.WILDCARD_PART;

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
     * NullAuthenticatedSessionStrategy() for bearer-only services.
     */
    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    /**
     * UserDetailsService that throws a UsernameNotFoundException for all requests.
     */
    @Bean
    public UserDetailsService emptyDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("no local users, only JWT tokens allowed");
        };
    }

    /**
     * Configure CORS & requests handling behaviour.
     **/
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http, @Value(
        "${security.cors.allowed-origins}"
    ) final String[] allowedOrigins) throws Exception {

        // State-less session (state in access-token only)
        http.sessionManagement(sessionManagement -> {
            sessionManagement.sessionAuthenticationStrategy(sessionAuthenticationStrategy());
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });

        // Return 401 (unauthorized) instead of 302 (redirect to login) when an authorization is missing or invalid.
        http.exceptionHandling(handler -> handler.authenticationEntryPoint((request, response, authException) -> {
            response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"Restricted Content\"");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }));

        // Disable CSRF because of state-less session-management
        http.csrf(AbstractHttpConfigurer::disable);

        // Enable CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource(allowedOrigins)));

        // Configure Endpoints
        http.authorizeHttpRequests(accessManagement -> {
            accessManagement.anyRequest().permitAll();
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
