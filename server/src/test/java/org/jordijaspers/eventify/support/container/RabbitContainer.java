package org.jordijaspers.eventify.support.container;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@TestConfiguration
public class RabbitContainer {

    private static final int DEFAULT_PORT = 5672;
    private static final int MANAGEMENT_PORT = 15672;
    private static final String DEFAULT_USER = "tst_user";
    private static final String DEFAULT_PASS = "tst_pass";

    @Container
    private static final RabbitMQContainer rabbitMQContainer;

    static {
        final DockerImageName image = DockerImageName.parse("rabbitmq:4-management");

        rabbitMQContainer = new RabbitMQContainer(image).withExposedPorts(DEFAULT_PORT, MANAGEMENT_PORT);
        rabbitMQContainer.setWaitStrategy(
            new LogMessageWaitStrategy()
                .withRegEx(".*Server startup complete.*\\s")
                .withTimes(1)
                .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
        );

        rabbitMQContainer.start();

        try {
            rabbitMQContainer.execInContainer(
                "rabbitmqadmin",
                "declare",
                "user",
                "name=" + DEFAULT_USER,
                "password=" + DEFAULT_PASS,
                "tags="
            );
            rabbitMQContainer.execInContainer(
                "rabbitmqadmin",
                "declare",
                "vhost=/",
                "user=" + DEFAULT_USER,
                "configure=.*",
                "write=.*",
                "read=.*"
            );
        } catch (final IOException | InterruptedException exception) {
            log.error("Failed to execute commands in RabbitMQ container", exception);
        } finally {
            log.debug(rabbitMQContainer.getLogs());
        }
    }

    @Bean
    @Primary
    @ServiceConnection
    public RabbitMQContainer getRabbitMQContainer() {
        return rabbitMQContainer;
    }
}
