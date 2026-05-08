package io.github.eventify.support.container;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

/**
 * Timescale container configuration.
 */
@Slf4j
@TestConfiguration
public class TimescaleContainer {

    public static final String DATABASE_NAME = "tst_db";
    private static final int MAX_CONNECTION_RETRIES = 10;
    private static final Duration RETRY_DELAY = Duration.ofMillis(500);

    @Container
    public static final PostgreSQLContainer<?> timescaleContainer;

    static {
        final DockerImageName image = DockerImageName.parse("timescale/timescaledb-ha:pg17")
            .asCompatibleSubstituteFor("postgres");

        timescaleContainer = new PostgreSQLContainer<>(image)
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_NAME)
            .withPassword(DATABASE_NAME)
            .withUrlParam("sslmode", "disable")
            .withEnv(
                Map.of(
                    "POSTGRES_HOST_AUTH_METHOD",
                    "trust",
                    "TZ",
                    "UTC",
                    "PGTZ",
                    "UTC"
                )
            );

        timescaleContainer.addExposedPort(5432);
        timescaleContainer.setWaitStrategy(getWaitStrategy());
        timescaleContainer.start();

        waitForJdbcConnection();
        log.debug(timescaleContainer.getLogs());
    }

    @Bean
    @Primary
    @ServiceConnection
    public PostgreSQLContainer<?> getTimescaleContainer() {
        return timescaleContainer;
    }

    /**
     * Creates a compound wait strategy that waits for both the PostgreSQL ready log message and for the listening port to be available.
     *
     * @return the wait strategy
     */
    private static WaitStrategy getWaitStrategy() {
        return new WaitAllStrategy()
            .withStrategy(
                new LogMessageWaitStrategy()
                    .withRegEx(".*database system is ready to accept connections.*\\s")
                    .withTimes(2)
                    .withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS))
            )
            .withStrategy(Wait.forListeningPort())
            .withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS));
    }

    /**
     * Waits for JDBC connection to be fully available. This ensures the database is truly ready to accept connections, not just that
     * PostgreSQL has logged the ready message.
     */
    private static void waitForJdbcConnection() {
        final String jdbcUrl = timescaleContainer.getJdbcUrl();
        final String username = timescaleContainer.getUsername();
        final String password = timescaleContainer.getPassword();

        for (int attempt = 1; attempt <= MAX_CONNECTION_RETRIES; attempt++) {
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                if (connection.isValid(5)) {
                    log.info("Database connection verified on attempt {}", attempt);
                    return;
                }
            } catch (final SQLException e) {
                log.warn("Database connection attempt {} failed: {}", attempt, e.getMessage());
                if (attempt < MAX_CONNECTION_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY.toMillis());
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Interrupted while waiting for database connection", ie);
                    }
                }
            }
        }
        throw new IllegalStateException("Failed to establish database connection after " + MAX_CONNECTION_RETRIES + " attempts");
    }
}
