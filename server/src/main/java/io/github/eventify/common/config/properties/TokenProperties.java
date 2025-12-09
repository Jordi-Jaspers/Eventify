package io.github.eventify.common.config.properties;

import lombok.Data;

import java.time.temporal.ChronoUnit;

/**
 * Configuration properties for token management.
 */
@Data
public class TokenProperties {

    private int lifetime;

    private ChronoUnit timeUnit;

}
