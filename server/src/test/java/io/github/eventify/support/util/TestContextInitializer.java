package io.github.eventify.support.util;

import io.github.eventify.Main;
import io.github.eventify.api.apikey.repository.ApiKeyAuditRepository;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.authentication.service.AuthenticationService;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.notification.repository.NotificationBroadcastRepository;
import io.github.eventify.api.notification.repository.NotificationRepository;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.quota.repository.UserEventQuotaRepository;
import io.github.eventify.api.token.repository.TokenRepository;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.mapper.UserDetailsMapper;
import io.github.eventify.api.user.repository.UserAuthProviderRepository;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.common.audit.repository.AuditLogRepository;
import io.github.eventify.support.config.BeanConfiguration;
import io.github.eventify.support.config.TimescaleLiquibaseConfiguration;
import io.github.eventify.support.container.TimescaleContainer;
import io.github.jframe.autoconfigure.properties.ApplicationProperties;
import tools.jackson.databind.ObjectMapper;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        TimescaleLiquibaseConfiguration.class,
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

    @NonNull
    @Autowired
    protected JdbcTemplate jdbcTemplate;

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

    @Autowired
    protected ApiKeyRepository apiKeyRepository;

    @Autowired
    protected ApiKeyAuditRepository apiKeyAuditRepository;

    @Autowired
    protected AuditLogRepository auditLogRepository;

    @Autowired
    protected ChannelRepository channelRepository;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected UserEventQuotaRepository userEventQuotaRepository;

    @Autowired
    protected WatchlistRepository watchlistRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected TestDataCleanupService testDataCleanupService;

    @Autowired
    protected UserAuthProviderRepository userAuthProviderRepository;

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected NotificationBroadcastRepository notificationBroadcastRepository;

}
