package org.jordijaspers.eventify.common.config.properties;

import lombok.Data;

import java.time.temporal.ChronoUnit;

@Data
public class TokenProperties {

    private int lifetime;

    private ChronoUnit timeUnit;

}
