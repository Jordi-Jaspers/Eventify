package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.response.AuditLogStatsResponse;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.audit.model.AuditLog;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import tools.jackson.core.type.TypeReference;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ADMIN_AUDIT_LOG_SEARCH_PATH;
import static io.github.eventify.api.Paths.ADMIN_AUDIT_LOG_STATS_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Integration Test - Admin Audit Log Controller")
public class AdminAuditLogControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return audit log page when admin searches with no filters")
    public void searchAuditLogSuccessNoFilters() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: Some audit log entries exist
        final User actor = aValidatedUser();
        anAuditLog(actor, "GET", "/api/v1/channels", (short) 200);
        anAuditLog(actor, "POST", "/api/v1/events", (short) 201);

        // When: Searching audit log with no filters
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain audit log entries
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(2L)));
    }

    @Test
    @DisplayName("Should return 403 when user without MANAGE_USERS authority searches audit log")
    public void searchAuditLogFailsWhenNotAdmin() throws Exception {
        // Given: A regular user without admin authority
        final User regularUser = aValidatedUser();

        // When: Attempting to search audit log
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated request searches audit log")
    public void searchAuditLogFailsWhenUnauthenticated() throws Exception {
        // Given: No authentication

        // When: Attempting to search audit log without token
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return empty page when no audit log entries exist")
    public void searchAuditLogReturnsEmptyWhenNoEntries() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: No audit log entries exist for this test (clean state)

        // When: Searching audit log
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final SearchInput actorFilter = new SearchInput();
        actorFilter.setFieldName("actor");
        actorFilter.setTextValue("nonexistent.actor.xyz987@integration.test");
        input.getSearchInputs().add(actorFilter);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should be empty
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should filter audit log by 2xx status range")
    public void searchAuditLogFiltersBySuccessStatus() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: Audit log entries with different status codes
        final User actor = aValidatedUser();
        anAuditLog(actor, "GET", "/api/v1/channels", (short) 200);
        anAuditLog(actor, "POST", "/api/v1/events", (short) 201);
        anAuditLog(actor, "GET", "/api/v1/missing", (short) 404);
        anAuditLog(actor, "POST", "/api/v1/error", (short) 500);

        // When: Searching with 2xx status filter
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName("status");
        statusFilter.setTextValueList(List.of("2xx"));
        input.getSearchInputs().add(statusFilter);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should only contain 2xx entries
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("\"statusCode\":200"));
        assertThat(content, not(containsString("\"statusCode\":404")));
        assertThat(content, not(containsString("\"statusCode\":500")));
    }

    @Test
    @DisplayName("Should filter audit log by 4xx status range")
    public void searchAuditLogFiltersByClientErrorStatus() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: Audit log entries with different status codes
        final User actor = aValidatedUser();
        anAuditLog(actor, "GET", "/api/v1/channels", (short) 200);
        anAuditLog(actor, "GET", "/api/v1/missing", (short) 404);
        anAuditLog(actor, "POST", "/api/v1/bad", (short) 400);

        // When: Searching with 4xx status filter
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName("status");
        statusFilter.setTextValueList(List.of("4xx"));
        input.getSearchInputs().add(statusFilter);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should only contain 4xx entries
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, not(containsString("\"statusCode\":200")));
        assertThat(content, containsString("\"statusCode\":40"));
    }

    @Test
    @DisplayName("Should filter audit log by multiple status ranges simultaneously")
    public void searchAuditLogFiltersByMultipleStatusRanges() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: Audit log entries with different status codes
        final User actor = aValidatedUser();
        anAuditLog(actor, "GET", "/api/v1/ok", (short) 200);
        anAuditLog(actor, "GET", "/api/v1/missing", (short) 404);
        anAuditLog(actor, "POST", "/api/v1/error", (short) 500);

        // When: Searching with both 2xx and 4xx status filters
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName("status");
        statusFilter.setTextValueList(List.of("2xx", "4xx"));
        input.getSearchInputs().add(statusFilter);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain 2xx and 4xx but not 5xx
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("\"statusCode\":200"));
        assertThat(content, containsString("\"statusCode\":404"));
        assertThat(content, not(containsString("\"statusCode\":500")));
    }

    @Test
    @DisplayName("Should filter audit log by actor name")
    public void searchAuditLogFiltersByActorName() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: Two actors with audit log entries
        final User actor1 = aValidatedUser();
        final User actor2 = aValidatedUser();
        anAuditLog(actor1, "GET", "/api/v1/channels", (short) 200);
        anAuditLog(actor2, "POST", "/api/v1/events", (short) 201);

        // When: Searching by actor1's first name
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final SearchInput actorFilter = new SearchInput();
        actorFilter.setFieldName("actor");
        actorFilter.setTextValue(actor1.getFirstName());
        input.getSearchInputs().add(actorFilter);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain entries from actor1
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});
        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(1L)));
    }

    @Test
    @DisplayName("Should return empty page when actor name does not match any user")
    public void searchAuditLogReturnsEmptyWhenActorNotFound() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: Some audit log entries exist
        final User actor = aValidatedUser();
        anAuditLog(actor, "GET", "/api/v1/channels", (short) 200);

        // When: Searching by a non-existent actor name
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final SearchInput actorFilter = new SearchInput();
        actorFilter.setFieldName("actor");
        actorFilter.setTextValue("NonExistentActorXYZ987");
        input.getSearchInputs().add(actorFilter);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should be empty
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});
        assertThat(pageResource.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should return paginated results when searching audit log")
    public void searchAuditLogReturnsPaginatedResults() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: Many audit log entries exist
        final User actor = aValidatedUser();
        for (int i = 0; i < 15; i++) {
            anAuditLog(actor, "GET", "/api/v1/channels/" + i, (short) 200);
        }

        // When: Requesting first page with size 5
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(5);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain limited results with correct total
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getContent().size(), is(lessThanOrEqualTo(5)));
        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(15L)));
    }

    @Test
    @DisplayName("Should return audit log stats when admin requests with valid time range")
    public void getAuditLogStatsSuccess() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: Some audit log entries exist
        final User actor = aValidatedUser();
        anAuditLog(actor, "GET", "/api/v1/channels", (short) 200);
        anAuditLog(actor, "POST", "/api/v1/events", (short) 201);
        anAuditLog(actor, "GET", "/api/v1/missing", (short) 404);

        // When: Requesting stats for a wide time range
        final String from = OffsetDateTime.now().minusDays(1).withOffsetSameInstant(java.time.ZoneOffset.UTC).toString();
        final String to = OffsetDateTime.now().plusDays(1).withOffsetSameInstant(java.time.ZoneOffset.UTC).toString();

        final MockHttpServletRequestBuilder request = get(ADMIN_AUDIT_LOG_STATS_PATH)
            .param("from", from)
            .param("to", to)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Stats should contain expected counts
        final String content = response.andReturn().getResponse().getContentAsString();
        final AuditLogStatsResponse stats = fromJson(content, AuditLogStatsResponse.class);

        assertThat(stats, is(notNullValue()));
        assertThat(stats.getTotalRequests(), is(greaterThanOrEqualTo(3L)));
        assertThat(stats.getErrorCount(), is(greaterThanOrEqualTo(1L)));
        assertThat(stats.getMutationCount(), is(greaterThanOrEqualTo(1L)));
        assertThat(stats.getUniqueActors(), is(greaterThanOrEqualTo(1L)));
        assertThat(stats.getHourlyBuckets(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return 403 when non-admin requests audit log stats")
    public void getAuditLogStatsFailsWhenNotAdmin() throws Exception {
        // Given: A regular user
        final User regularUser = aValidatedUser();

        // When: Requesting stats
        final String from = OffsetDateTime.now().minusDays(1).withOffsetSameInstant(java.time.ZoneOffset.UTC).toString();
        final String to = OffsetDateTime.now().plusDays(1).withOffsetSameInstant(java.time.ZoneOffset.UTC).toString();

        final MockHttpServletRequestBuilder request = get(ADMIN_AUDIT_LOG_STATS_PATH)
            .param("from", from)
            .param("to", to)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated request gets audit log stats")
    public void getAuditLogStatsFailsWhenUnauthenticated() throws Exception {
        // Given: No authentication

        // When: Requesting stats without token
        final String from = OffsetDateTime.now().minusDays(1).withOffsetSameInstant(java.time.ZoneOffset.UTC).toString();
        final String to = OffsetDateTime.now().plusDays(1).withOffsetSameInstant(java.time.ZoneOffset.UTC).toString();

        final MockHttpServletRequestBuilder request = get(ADMIN_AUDIT_LOG_STATS_PATH)
            .param("from", from)
            .param("to", to);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should exclude paths matching excludePath filter")
    public void searchAuditLogExcludesMatchingPaths() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: Audit log entries with different paths
        final User actor = aValidatedUser();
        anAuditLog(actor, "GET", "/api/v1/channels", (short) 200);
        anAuditLog(actor, "GET", "/actuator/health", (short) 200);

        // When: Searching with excludePath filter for /actuator
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final SearchInput excludeFilter = new SearchInput();
        excludeFilter.setFieldName("excludePath");
        excludeFilter.setTextValue("/actuator");
        input.getSearchInputs().add(excludeFilter);

        final MockHttpServletRequestBuilder request = post(ADMIN_AUDIT_LOG_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should not contain /actuator paths
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, not(containsString("/actuator")));
        assertThat(content, containsString("/api/v1/channels"));
    }

    // ========================= HELPER METHODS =========================

    private AuditLog anAuditLog(final User actor, final String method, final String path, final short statusCode) {
        final AuditLog log = new AuditLog(
            actor,
            method,
            path,
            statusCode,
            null,
            "127.0.0.1",
            OffsetDateTime.now()
        );
        return auditLogRepository.save(log);
    }
}
