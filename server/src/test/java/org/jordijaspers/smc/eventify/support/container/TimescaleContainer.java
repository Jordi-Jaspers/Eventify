package org.jordijaspers.smc.eventify.support.container;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Timescale container configuration.
 */
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class TimescaleContainer {

    public static final String DATABASE_NAME = "tst_eventify";

    @ServiceConnection
    private static final PostgreSQLContainer<?> timescaleContainer;

    static {
        final DockerImageName image = DockerImageName.parse("timescale/timescaledb-ha:pg17")
            .asCompatibleSubstituteFor("postgres");

        timescaleContainer = new PostgreSQLContainer<>(image)
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_NAME)
            .withPassword(DATABASE_NAME)
            .withUrlParam("sslmode", "disable")
            .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust");

        timescaleContainer.setWaitStrategy(
            new LogMessageWaitStrategy()
                .withRegEx(".*database system is ready to accept connections.*\\s")
                .withTimes(1)
                .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
        );

        timescaleContainer.start();
        log.debug(timescaleContainer.getLogs());
    }

    @Bean
    PostgreSQLContainer<?> timescaleContainer() {
        return timescaleContainer;
    }

}
