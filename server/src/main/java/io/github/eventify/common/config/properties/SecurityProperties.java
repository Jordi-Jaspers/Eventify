package io.github.eventify.common.config.properties;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * Security related properties.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * Allowed origin patterns for CORS (e.g., "http://localhost:*").
     * Supports wildcards when used with allowCredentials=true.
     */
    @Value("${security.cors.allowed-origins}")
    private String[] allowedOrigins = new String[] {
        "http://localhost:*"
    };

    private TokenProperties accessToken;

    private TokenProperties refreshToken;

    private BootstrapProperties bootstrap;

}
