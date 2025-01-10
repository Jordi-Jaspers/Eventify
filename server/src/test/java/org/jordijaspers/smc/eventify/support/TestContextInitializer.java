package org.jordijaspers.smc.eventify.support;

import org.jordijaspers.eventify.Application;
import org.jordijaspers.eventify.api.authentication.service.AuthenticationService;
import org.jordijaspers.eventify.api.token.repository.TokenRepository;
import org.jordijaspers.eventify.api.user.model.mapper.UserMapper;
import org.jordijaspers.eventify.api.user.repository.UserRepository;
import org.jordijaspers.eventify.api.user.service.UserService;
import org.jordijaspers.smc.eventify.support.config.BeanConfiguration;
import org.jordijaspers.smc.eventify.support.container.TimescaleContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Base class for integration tests. This class is used to import the necessary configurations for the tests.
 */
@Import(
    {
        BeanConfiguration.class,
        TimescaleContainer.class
    }
)
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(
    classes = Application.class,
    webEnvironment = RANDOM_PORT
)
public class TestContextInitializer {

    // ========================= CONTAINERS =========================
    @Autowired
    protected PostgreSQLContainer<?> timescaleContainer;

    // ========================= CONTEXT =========================
    @Autowired
    protected WebApplicationContext applicationContext;

    @Autowired
    protected HawaiiFilters hawaiiFilters;

    // ========================= APPLICATION =========================

    @Autowired
    protected AuthenticationService authenticationService;

    @Autowired
    protected UserMapper userMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UserService userService;

    @Autowired
    protected TokenRepository tokenRepository;
}
