package org.jordijaspers.eventify.support.util;

import org.jordijaspers.eventify.Application;
import org.jordijaspers.eventify.api.authentication.service.AuthenticationService;
import org.jordijaspers.eventify.api.check.service.CheckService;
import org.jordijaspers.eventify.api.dashboard.repository.DashboardRepository;
import org.jordijaspers.eventify.api.dashboard.service.DashboardService;
import org.jordijaspers.eventify.api.event.repository.EventRepository;
import org.jordijaspers.eventify.api.event.service.EventService;
import org.jordijaspers.eventify.api.monitoring.service.TimelineStreamingService;
import org.jordijaspers.eventify.api.source.service.SourceService;
import org.jordijaspers.eventify.api.team.repository.TeamRepository;
import org.jordijaspers.eventify.api.team.service.TeamService;
import org.jordijaspers.eventify.api.token.repository.TokenRepository;
import org.jordijaspers.eventify.api.user.model.mapper.UserMapper;
import org.jordijaspers.eventify.api.user.repository.UserRepository;
import org.jordijaspers.eventify.api.user.service.UserService;
import org.jordijaspers.eventify.support.config.BeanConfiguration;
import org.jordijaspers.eventify.support.container.RabbitContainer;
import org.jordijaspers.eventify.support.container.TimescaleContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Base class for integration tests. This class is used to import the necessary configurations for the tests.
 */
@Import(
    {
        BeanConfiguration.class,
        TimescaleContainer.class,
        RabbitContainer.class
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

    @Autowired
    protected RabbitMQContainer rabbitContainer;

    // ========================= CONTEXT =========================
    @Autowired
    protected WebApplicationContext applicationContext;

    @Autowired
    protected HawaiiFilters hawaiiFilters;

    @Autowired
    protected ObjectMapper objectMapper;

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

    @Autowired
    protected TeamRepository teamRepository;

    @Autowired
    protected TeamService teamService;

    @Autowired
    protected TimelineStreamingService timelineStreamingService;

    @Autowired
    protected DashboardRepository dashboardRepository;

    @Autowired
    protected DashboardService dashboardService;

    @Autowired
    protected SourceService sourceService;

    @Autowired
    protected CheckService checkService;

    @Autowired
    protected EventService eventService;

    @Autowired
    protected EventRepository eventRepository;
}
