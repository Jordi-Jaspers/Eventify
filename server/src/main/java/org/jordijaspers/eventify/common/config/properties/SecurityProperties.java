package org.jordijaspers.eventify.common.config.properties;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    @Value("${security.cors.allowed-origins}")
    private String[] allowedOrigins;

    private TokenProperties accessToken;

    private TokenProperties refreshToken;

}
