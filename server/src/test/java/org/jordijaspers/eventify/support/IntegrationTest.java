package org.jordijaspers.eventify.support;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hawaiiframework.repository.DataNotFoundException;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.authentication.model.request.RegisterUserRequest;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.request.CreateDashboardRequest;
import org.jordijaspers.eventify.api.dashboard.model.request.DashboardConfigurationRequest;
import org.jordijaspers.eventify.api.event.model.Status;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.source.model.request.CreateSourceRequest;
import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.team.model.request.TeamRequest;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.token.model.TokenType;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.model.request.*;
import org.jordijaspers.eventify.support.util.WebMvcConfigurator;
import org.junit.jupiter.api.BeforeEach;

import static org.jordijaspers.eventify.api.dashboard.service.DashboardService.configureGroupedChecks;
import static org.jordijaspers.eventify.api.dashboard.service.DashboardService.configureUngroupedChecks;
import static org.jordijaspers.eventify.api.token.model.TokenType.REFRESH_TOKEN;
import static org.jordijaspers.eventify.api.token.model.TokenType.USER_VALIDATION_TOKEN;
import static org.jordijaspers.eventify.common.constants.Constants.DateTime.EUROPE_AMSTERDAM;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.TOKEN_NOT_FOUND_ERROR;

/**
 * Base class for integration tests. This class is used to create helper methods for integration tests.
 */
public class IntegrationTest extends WebMvcConfigurator {

    protected static final String FIRST_NAME = "Eventify";
    protected static final String LAST_NAME = "User";
    protected static final String TEST_EMAIL = "eventify.user@integration.test";
    protected static final String TEST_PASSWORD = "Test123!@#";
    protected static final String INTEGRATION_PREFIX = "[Integration Test] - ";

    protected static final String TEAM_NAME = "Web Integration Test Team";
    protected static final String TEAM_DESCRIPTION = "Description for the Web Integration Test Team";

    protected static final String NEW_PASSWORD = "NewTest123!@#";
    protected static final String NEW_PASSWORD_CONFIRMATION = "NewTest123!@#";

    protected static final String CHECK_NAME = "Generated Check";
    protected static final String SOURCE_NAME = "ITEST Source";
    protected static final String DASHBOARD_NAME = "ITEST Dashboard";

    @BeforeEach
    public void cleanUp() {
        deleteAllSources();
        deleteAllTestDashboard();
        deleteAllTestTeams();
        deleteAllTestUsers();
    }

    protected EventRequest anEventRequest(final Long checkId, final Status status) {
        return new EventRequest()
            .setCheckId(checkId)
            .setStatus(status)
            .setMessage(INTEGRATION_PREFIX + "Event message")
            .setCorrelationId(INTEGRATION_PREFIX + checkId)
            .setTimestamp(ZonedDateTime.now(EUROPE_AMSTERDAM));
    }

    protected void generateEvents(final Long checkId, final Status... statuses) {
        final ZonedDateTime now = ZonedDateTime.now(EUROPE_AMSTERDAM);
        final ZonedDateTime start = now.minusHours(2);
        final long minutesPerStatus = Duration.between(start, now).toMinutes() / statuses.length;

        for (int i = 0; i < statuses.length; i++) {
            final EventRequest request = anEventRequest(checkId, statuses[i]);
            request.setTimestamp(start.plusMinutes(minutesPerStatus * (i + 1)));
            eventService.createEvent(request);
        }
    }

    protected Dashboard configureDashboard(final Long dashboardId, final DashboardConfigurationRequest request) {
        final Dashboard dashboard = dashboardService.getDashboardConfiguration(dashboardId);
        dashboard.clearConfiguration();
        dashboardRepository.saveAndFlush(dashboard);

        configureGroupedChecks(request.getGroups(), dashboard);
        configureUngroupedChecks(request, dashboard);
        dashboard.setUpdatedBy(TEST_EMAIL);
        dashboard.setLastUpdated(LocalDateTime.now());

        return dashboardRepository.save(dashboard);
    }

    protected CreateDashboardRequest aValidCreateDashboardRequest(final Team team) {
        return new CreateDashboardRequest()
            .setName(UUID.randomUUID() + "-" + DASHBOARD_NAME)
            .setDescription("Description for the global dashboard")
            .setTeamId(team.getId())
            .setGlobal(false);
    }

    protected Dashboard aValidDashboard(final Team team, final boolean global) {
        final CreateDashboardRequest request = aValidCreateDashboardRequest(team);
        request.setGlobal(global);

        final Dashboard dashboard = new Dashboard(request, TEST_EMAIL, team);
        dashboard.setUpdatedBy(TEST_EMAIL);
        dashboard.setLastUpdated(LocalDateTime.now());

        return dashboardRepository.save(dashboard);
    }

    protected Dashboard aValidDashboard(final Team team) {
        return aValidDashboard(team, false);
    }

    protected Source aValidSource() {
        final CreateSourceRequest request = new CreateSourceRequest()
            .setName(UUID.randomUUID() + "-" + SOURCE_NAME)
            .setDescription("Test Source");

        return sourceService.createSource(request);
    }

    protected List<Check> generateChecks(final Source source, final int amount) {
        final Set<Check> checks = new HashSet<>();
        for (int i = 0; i < amount; i++) {
            checks.add(checkService.createCheck(UUID.randomUUID() + "-" + CHECK_NAME, source));
        }
        return List.copyOf(checks);
    }

    protected Check aValidCheck(final Source source) {
        return checkService.createCheck(UUID.randomUUID() + "-" + CHECK_NAME, source);
    }

    protected Team aValidTeam() {
        final TeamRequest request = aTeamRequest();
        return teamService.createTeam(request);
    }

    protected User aValidatedUserWithAuthority(final Authority authority) {
        final User user = aValidatedUser();
        updateUserAuthority(user, authority);
        return authenticationService.refresh(user.getRefreshToken().getValue());
    }

    protected void updateUserAuthority(final User user, final Authority authority) {
        userService.updateAuthority(user.getId(), authority);
    }

    protected void addUserToTeam(final User user, final Team team) {
        teamService.addMembers(team.getId(), Set.of(user.getId()));
    }

    protected Team AValidTeamWithMembers(final int amount) {
        final Team team = aValidTeam();
        final Set<Long> userIds = new HashSet<>();

        for (int i = 0; i < amount; i++) {
            final User user = aValidatedUser();
            userIds.add(user.getId());
        }

        return teamService.addMembers(team.getId(), userIds);
    }

    protected User anUnvalidatedUser() {
        final RegisterUserRequest registerRequest = aRegisterRequest();
        return authenticationService.register(userMapper.toUser(registerRequest), registerRequest.getPassword());
    }

    protected User aValidatedUser() {
        final User user = anUnvalidatedUser();
        final Token token = getValidationToken(user);
        return authenticationService.verifyEmail(token.getValue());
    }

    protected User aLockedUser() {
        final User user = aValidatedUser();
        return userService.lockUser(user.getId(), true);
    }

    protected UpdateAuthorityRequest anUpdateAuthorityRequest(final Authority authority) {
        return new UpdateAuthorityRequest()
            .setAuthority(authority);
    }

    protected static TeamRequest aTeamRequest() {
        return new TeamRequest()
            .setName(INTEGRATION_PREFIX + UUID.randomUUID() + " - " + TEAM_NAME)
            .setDescription(TEAM_DESCRIPTION);
    }

    protected static RegisterUserRequest aRegisterRequest() {
        final String prefix = UUID.randomUUID().toString().substring(0, 5);
        return new RegisterUserRequest()
            .setEmail(prefix + "." + TEST_EMAIL)
            .setPassword(TEST_PASSWORD)
            .setPasswordConfirmation(TEST_PASSWORD)
            .setFirstName(FIRST_NAME)
            .setLastName(LAST_NAME);
    }

    protected static ForgotPasswordRequest aForgotPasswordRequest() {
        final ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setNewPassword(NEW_PASSWORD);
        request.setConfirmPassword(NEW_PASSWORD_CONFIRMATION);
        request.setToken(UUID.randomUUID().toString());
        return request;
    }

    protected static UpdatePasswordRequest anUpdatePasswordRequest() {
        final UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword(TEST_PASSWORD);
        request.setNewPassword(NEW_PASSWORD);
        request.setConfirmPassword(NEW_PASSWORD_CONFIRMATION);
        return request;
    }

    protected UpdateUserDetailsRequest anUpdateUserDetailsRequest() {
        return new UpdateUserDetailsRequest()
            .setFirstName("Updated")
            .setLastName("User");
    }

    protected UpdateEmailRequest anUpdateEmailRequest() {
        final String prefix = UUID.randomUUID().toString().substring(0, 5);
        return new UpdateEmailRequest()
            .setEmail(prefix + TEST_EMAIL);
    }

    protected Token getPasswordResetToken(final User user) {
        return tokenRepository.findByEmail(user.getEmail())
            .stream()
            .filter(token -> token.getType().equals(TokenType.RESET_PASSWORD_TOKEN))
            .findFirst()
            .orElseThrow(() -> new DataNotFoundException(TOKEN_NOT_FOUND_ERROR));
    }

    protected Token getValidationToken(final User user) {
        return tokenRepository.findByEmail(user.getEmail())
            .stream()
            .filter(entry -> entry.getType().equals(USER_VALIDATION_TOKEN))
            .findFirst()
            .orElseThrow(() -> new DataNotFoundException(TOKEN_NOT_FOUND_ERROR));
    }

    protected Token getRefreshToken(final User user) {
        return tokenRepository.findByEmail(user.getEmail())
            .stream()
            .filter(entry -> entry.getType().equals(REFRESH_TOKEN))
            .findFirst()
            .orElseThrow(() -> new DataNotFoundException(TOKEN_NOT_FOUND_ERROR));
    }

    protected User getUserDetails(final String email) {
        return userService.loadUserByUsername(email);
    }

    private void deleteAllSources() {
        final List<Source> sources = sourceRepository.findAllByNameContaining(SOURCE_NAME);
        sourceRepository.deleteAll(sources);
    }

    private void deleteAllTestUsers() {
        final List<User> users = userRepository.findAllByEmailContaining(TEST_EMAIL);
        userRepository.deleteAll(users);
    }

    private void deleteAllTestTeams() {
        final List<Team> teams = teamRepository.findAllByNameContaining(TEAM_NAME);
        teamRepository.deleteAll(teams);
    }

    private void deleteAllTestDashboard() {
        final List<Dashboard> dashboards = dashboardRepository.findAllByNameContaining(DASHBOARD_NAME);
        dashboardRepository.deleteAll(dashboards);
    }
}
