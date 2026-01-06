package io.github.eventify.support.util;

import io.github.eventify.Main;
import io.github.eventify.api.authentication.service.AuthenticationService;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.token.repository.TokenRepository;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.mapper.UserDetailsMapper;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.support.config.BeanConfiguration;
import io.github.eventify.support.container.TimescaleContainer;
import io.github.jframe.autoconfigure.properties.ApplicationProperties;
import tools.jackson.databind.ObjectMapper;

import org.jspecify.annotations.NonNull;
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
@ActiveProfiles(
    {
        "test",
        "console"
    }
)
@SpringBootTest(
    classes = Main.class,
    webEnvironment = RANDOM_PORT
)
public class TestContextInitializer {

    // ========================= CONTEXT =========================
    @NonNull
    @Autowired
    protected WebApplicationContext applicationContext;

    @NonNull
    @Autowired
    protected LoggingFilters loggingFilters;

    @NonNull
    @Autowired
    protected ObjectMapper objectMapper;

    // ========================= CONTAINERS =========================
    @Autowired
    protected PostgreSQLContainer<?> postgreSQLContainer;

    // ========================= APPLICATION =========================

    @Autowired
    protected ApplicationProperties applicationProperties;

    @Autowired
    protected AuthenticationService authenticationService;

    @Autowired
    protected UserDetailsMapper userDetailsMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected OrganizationRepository organizationRepository;

    @Autowired
    protected OrganizationMembershipRepository organizationMembershipRepository;

    @Autowired
    protected UserService userService;

    @Autowired
    protected TokenRepository tokenRepository;

    @Autowired
    protected TokenService tokenService;

}
