package org.jordijaspers.smc.eventify.support.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration {

    public static final String DATABASE_NAME = "tst_eventify";

    private static final PostgreSQLContainer<?> timescaleContainer;

    static {
        final DockerImageName image = DockerImageName.parse("timescale/timescaledb-ha:pg17")
            .asCompatibleSubstituteFor("postgres");

        timescaleContainer = new PostgreSQLContainer<>(image)
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_NAME)
            .withPassword(DATABASE_NAME);

        timescaleContainer.setWaitStrategy(
            new LogMessageWaitStrategy()
                .withRegEx(".*database system is ready to accept connections.*\\s")
                .withTimes(1)
                .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
        );

        timescaleContainer.start();
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> timescaleContainer() {
        return timescaleContainer;
    }
}
