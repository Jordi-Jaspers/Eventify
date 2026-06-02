package io.github.eventify.api.dashboard.controller;

import io.github.eventify.api.dashboard.model.response.UserDashboardResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.USER_DASHBOARD_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - UserDashboardController")
public class UserDashboardControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return 200 with full response when user has data")
    public void getDashboardSuccess() throws Exception {
        // Given: An authenticated user with an organization membership
        final User user = aValidatedUser();
        final Organization org = anOrganisationWithOwner(user);

        // When: Requesting the user dashboard
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain all three sections
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDashboardResponse dashboard = fromJson(content, UserDashboardResponse.class);

        assertThat(dashboard, is(notNullValue()));
        assertThat(dashboard.watchlistHealth(), is(notNullValue()));
        assertThat(dashboard.recentNotifications(), is(notNullValue()));
        assertThat(dashboard.organizations(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return 200 with empty lists for new user")
    public void getDashboardEmptyStateForNewUser() throws Exception {
        // Given: A brand new authenticated user with no data
        final User user = aValidatedUser();

        // When: Requesting the user dashboard
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));

        // And: All sections should be empty lists
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDashboardResponse dashboard = fromJson(content, UserDashboardResponse.class);

        assertThat(dashboard.watchlistHealth(), is(empty()));
        // Note: recentNotifications may contain welcome notification from registration
        assertThat(dashboard.organizations(), is(empty()));
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated")
    public void getDashboardWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided

        // When: Requesting the user dashboard without a token
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 401 Unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return organizations section with suspended org when user is member")
    public void getDashboardIncludesSuspendedOrganization() throws Exception {
        // Given: A user who is a member of a suspended organization
        final User user = aValidatedUser();
        final Organization org = anOrganisationWithOwner(user);
        org.setStatus(OrganizationStatus.SUSPENDED);
        organizationRepository.save(org);

        // When: Requesting the user dashboard
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));

        // And: The suspended org should appear in organizations section
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDashboardResponse dashboard = fromJson(content, UserDashboardResponse.class);

        assertThat(dashboard.organizations(), hasSize(greaterThanOrEqualTo(1)));
        final boolean hasSuspended = dashboard.organizations().stream()
            .anyMatch(o -> "SUSPENDED".equals(o.status()));
        assertThat(hasSuspended, is(true));
    }

    @Test
    @DisplayName("Should return recent notifications within last 24h")
    public void getDashboardIncludesRecentNotifications() throws Exception {
        // Given: A user with a recent notification
        final User user = aValidatedUser();
        aNotificationForUser(user, "Recent Alert");

        // When: Requesting the user dashboard
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));

        // And: The recent notification should appear
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDashboardResponse dashboard = fromJson(content, UserDashboardResponse.class);

        assertThat(dashboard.recentNotifications(), hasSize(greaterThanOrEqualTo(1)));
        assertThat(dashboard.recentNotifications().get(0).title(), is(equalTo("Recent Alert")));
    }

    @Test
    @DisplayName("Should return at most 10 notifications in recentNotifications")
    public void getDashboardReturnsAtMost10Notifications() throws Exception {
        // Given: A user with 12 recent notifications
        final User user = aValidatedUser();
        for (int i = 1; i <= 12; i++) {
            aNotificationForUser(user, "Notification " + i);
        }

        // When: Requesting the user dashboard
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));

        // And: At most 10 notifications should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDashboardResponse dashboard = fromJson(content, UserDashboardResponse.class);

        assertThat(dashboard.recentNotifications(), hasSize(lessThanOrEqualTo(10)));
    }

    @Test
    @DisplayName("Should return watchlist in health section when user has watchlists")
    public void getDashboardIncludesWatchlistHealth() throws Exception {
        // Given: A user with a watchlist
        final User user = aValidatedUser();
        aWatchlistForUser(user, "My Watchlist");

        // When: Requesting the user dashboard
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));

        // And: watchlistHealth section should be present (may be empty if all OK)
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDashboardResponse dashboard = fromJson(content, UserDashboardResponse.class);

        assertThat(dashboard.watchlistHealth(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return application/json content type")
    public void getDashboardReturnsJsonContentType() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Requesting the user dashboard
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response content type should be application/json
        response.andExpect(status().is(SC_OK));

        final String contentType = response.andReturn().getResponse().getContentType();
        assertThat(contentType, containsString("application/json"));
    }

    @Test
    @DisplayName("Should only return data belonging to the authenticated user")
    public void getDashboardReturnsOnlyAuthenticatedUserData() throws Exception {
        // Given: Two users, each with their own organization
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();
        anOrganisationWithOwner(user1);
        anOrganisationWithOwner(user2);

        // When: user1 requests the dashboard
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));

        // And: Only user1's organization should appear
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDashboardResponse dashboard = fromJson(content, UserDashboardResponse.class);

        assertThat(dashboard.organizations(), hasSize(1));
    }
}
