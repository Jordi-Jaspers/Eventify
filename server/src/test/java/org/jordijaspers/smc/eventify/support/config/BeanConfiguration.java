package org.jordijaspers.smc.eventify.support.config;

import org.jordijaspers.smc.eventify.support.HawaiiFilters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
public class BeanConfiguration {

    @Bean
    @Profile("test")
    public HawaiiFilters hawaiiFilters() {
        return new HawaiiFilters();
    }

}
