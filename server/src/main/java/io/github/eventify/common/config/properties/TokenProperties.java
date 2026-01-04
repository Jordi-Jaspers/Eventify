package io.github.eventify.common.config.properties;

import lombok.Data;

import java.time.temporal.ChronoUnit;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for token management.
 */
@Data
@Configuration
public class TokenProperties {

    private int lifetime;

    private ChronoUnit timeUnit;

}
