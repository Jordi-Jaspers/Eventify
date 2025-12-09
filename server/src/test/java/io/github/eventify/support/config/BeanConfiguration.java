package io.github.eventify.support.config;

import io.github.eventify.support.util.LoggingFilters;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for the beans used in the tests.
 */
@TestConfiguration
public class BeanConfiguration {

    @Bean
    @Profile("test")
    public LoggingFilters loggingFilters() {
        return new LoggingFilters();
    }

}
