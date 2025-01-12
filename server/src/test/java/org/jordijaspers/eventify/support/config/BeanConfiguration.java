package org.jordijaspers.eventify.support.config;

import org.jordijaspers.eventify.support.util.HawaiiFilters;
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
    public HawaiiFilters hawaiiFilters() {
        return new HawaiiFilters();
    }

}
