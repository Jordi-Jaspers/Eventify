package io.github.eventify.common.config;

import io.github.eventify.common.audit.filter.AdminRequestCachingFilter;
import io.github.eventify.common.config.properties.SecurityProperties;
import io.github.eventify.common.security.filter.ApiKeyAuthenticationFilter;
import io.github.eventify.common.security.filter.JwtAuthenticationFilter;
import io.github.eventify.common.security.oauth2.CustomOAuth2AuthorizationRequestResolver;
import io.github.eventify.common.security.oauth2.CustomOAuth2UserService;
import io.github.eventify.common.security.oauth2.OAuth2AttributesFilter;
import io.github.eventify.common.security.oauth2.OAuth2AuthenticationFailureHandler;
import io.github.eventify.common.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.security.filter.UnauthorizedHandler.handleAuthenticationFailure;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Configures spring web security.
 */
@SuppressWarnings("PMD.ExcessiveImports")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true
)
@RequiredArgsConstructor
public class WebSecurityConfig implements WebMvcConfigurer {

    private final SecurityProperties securityProperties;

    private final OAuth2AttributesFilter oauth2AttributesFilter;

    /**
     * Configure CORS & requests handling behaviour.
     **/
    @Bean
    @SuppressWarnings("checkstyle:ParameterNumber")
    public SecurityFilterChain filterChain(
        final AdminRequestCachingFilter adminRequestCachingFilter,
        final JwtAuthenticationFilter jwtAuthenticationFilter,
        final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
        final CustomOAuth2UserService customOAuth2UserService,
        final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
        final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
        final ClientRegistrationRepository clientRegistrationRepository,
        final HttpSecurity http) throws Exception {
        http.addFilterBefore(jwtAuthenticationFilter, OAuth2AuthorizationRequestRedirectFilter.class);
        http.addFilterBefore(adminRequestCachingFilter, JwtAuthenticationFilter.class);
        http.addFilterAfter(apiKeyAuthenticationFilter, JwtAuthenticationFilter.class);
        http.addFilterBefore(oauth2AttributesFilter, OAuth2LoginAuthenticationFilter.class);

        http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::disable);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(sessionConfiguration -> sessionConfiguration.sessionCreationPolicy(STATELESS));
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        http.exceptionHandling(
            handler -> handler.authenticationEntryPoint(
                (request, response, authException) -> handleAuthenticationFailure(
                    request,
                    response,
                    authException.getMessage()
                )
            )
        );

        http.oauth2Login(
            oauth2 -> oauth2
                .authorizationEndpoint(
                    endpointConfig -> endpointConfig
                        .baseUri(OAUTH2_AUTHORIZATION_PATH)
                        .authorizationRequestResolver(
                            new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository, OAUTH2_AUTHORIZATION_PATH)
                        )
                )
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
        );

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

    /**
     * Configures CORS settings for the application.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(securityProperties.getAllowedOrigins()));
        configuration.setAllowedMethods(List.of(WILDCARD));
        configuration.setAllowedHeaders(List.of(WILDCARD));
        configuration.setExposedHeaders(List.of(WILDCARD));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(WILDCARD_PART, configuration);
        return source;
    }
}
