package io.github.eventify.common.config;

import io.github.eventify.common.config.properties.SecurityProperties;
import io.github.eventify.common.security.filter.JwtAuthenticationFilter;
import io.github.eventify.common.security.oauth2.CustomOAuth2UserService;
import io.github.eventify.common.security.oauth2.OAuth2AuthenticationFailureHandler;
import io.github.eventify.common.security.oauth2.OAuth2AuthenticationSuccessHandler;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.security.filter.UnauthorizedHandler.handleAuthenticationFailure;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Configures spring web security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true
)
public class WebSecurityConfig implements WebMvcConfigurer {

    /**
     * Configure CORS & requests handling behaviour.
     **/
    @Bean
    public SecurityFilterChain filterChain(
        final SecurityProperties securityProperties,
        final JwtAuthenticationFilter jwtAuthenticationFilter,
        final CustomOAuth2UserService customOAuth2UserService,
        final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
        final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
        final HttpSecurity http) throws Exception {
        // Adding a once per request filter to check the JWT token.
        http.addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Configure Session Management.
        http.sessionManagement(sessionConfiguration -> sessionConfiguration.sessionCreationPolicy(STATELESS));

        // Return 401 (unauthorized) instead of 302 (redirect to login) when an authorization is missing or invalid.
        http.exceptionHandling(
            handler -> handler.authenticationEntryPoint(
                (request, response, authException) -> handleAuthenticationFailure(
                    request,
                    response,
                    authException.getMessage()
                )
            )
        );

        // Disable CSRF because of state-less session-management.
        http.csrf(AbstractHttpConfigurer::disable);

        // Enable CORS.
        http.cors(cors -> cors.configurationSource(corsConfigurationSource(securityProperties.getAllowedOrigins())));

        // Configure OAuth2 Resource Server.
        http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::disable);

        // Configure OAuth2 Login
        http.oauth2Login(
            oauth2 -> oauth2
                .authorizationEndpoint(endpointConfig -> endpointConfig.baseUri(OAUTH2_AUTHORIZATION_PATH))
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
        );

        // Configure Endpoints
        http.authorizeHttpRequests(accessManagement -> {
            accessManagement.requestMatchers(PUBLIC_ACTUATOR_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(PUBLIC_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(OPENAPI_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(PUBLIC_ERROR_PATH).permitAll();

            accessManagement.requestMatchers(AUTH_PATH + WILDCARD_PART).permitAll();
            accessManagement.requestMatchers(OAUTH2_PATH + WILDCARD_PART).permitAll();

            accessManagement.requestMatchers(LOGOUT_PATH).authenticated();
            accessManagement.anyRequest().authenticated();
        });

        return http.build();
    }

    private UrlBasedCorsConfigurationSource corsConfigurationSource(final String... origins) {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(origins));
        configuration.setAllowedMethods(List.of(WILDCARD));
        configuration.setAllowedHeaders(List.of(WILDCARD));
        configuration.setExposedHeaders(List.of(WILDCARD));
        configuration.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(WILDCARD_PART, configuration);
        return source;
    }
}
