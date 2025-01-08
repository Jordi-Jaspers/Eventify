package org.jordijaspers.smc.eventify.support.config;

import org.jordijaspers.smc.eventify.support.WebMvcConfigurator;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;

@Disabled
@Import(ContainersConfiguration.class)
public class DependencyConfig extends WebMvcConfigurator {

    @Autowired
    protected PostgreSQLContainer<?> timescaleContainer;

}
