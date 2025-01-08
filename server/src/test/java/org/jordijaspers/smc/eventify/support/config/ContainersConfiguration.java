package org.jordijaspers.smc.eventify.support.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration {

    public static final String LIQUIBASE_PROJECT_PATH = "../liquibase";

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
        //        runLiquibaseUpdate();
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> timescaleContainer() {
        return timescaleContainer;
    }

    //    private static void runLiquibaseUpdate() {
    //        final ProcessBuilder processBuilder = getProcessBuilder();
    //        try {
    //            Process process = processBuilder.start();
    //            int exitCode = process.waitFor();
    //            if (exitCode != 0) {
    //                throw new RuntimeException("Liquibase update failed with exit code: " + exitCode);
    //            }
    //        } catch (final InterruptedException | IOException e) {
    //            throw new RuntimeException("Failed to run Liquibase update", e);
    //        }
    //    }
    //
    //    private static ProcessBuilder getProcessBuilder() {
    //        final ProcessBuilder processBuilder = new ProcessBuilder();
    //        processBuilder.directory(new File(LIQUIBASE_PROJECT_PATH));
    //
    //        // Construct the Gradle command with necessary parameters
    //        List<String> command = new ArrayList<>();
    //        command.add("./gradlew");
    //        command.add("-Denv=custom");
    //        command.add("-Dcontexts=test");
    //        command.add("-DdbUrl=" + timescaleContainer.getJdbcUrl());
    //        command.add("-DdbUsername=" + timescaleContainer.getUsername());
    //        command.add("-DdbPassword=" + timescaleContainer.getPassword());
    //        command.add("-DchangelogFile=database/db.changelog.yaml");
    //        command.add("update");
    //
    //        // Add the command and show the logs in the console
    //        processBuilder.command(command);
    //        processBuilder.inheritIO();
    //        return processBuilder;
    //    }
}
