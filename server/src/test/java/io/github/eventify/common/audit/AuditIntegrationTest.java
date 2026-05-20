package io.github.eventify.common.audit;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.audit.event.AuditEvent;
import io.github.eventify.common.audit.model.AuditLog;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ADMIN_USERS_SEARCH_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Audit Infrastructure")
public class AuditIntegrationTest extends IntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("Should persist audit log when admin performs GET request")
    public void shouldPersistAuditLogWhenAdminPerformsGetRequest() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Admin performs a GET request to an admin endpoint
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH.replace("/search", ""))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        mockMvc.perform(request);

        // Then: An audit log entry should be persisted
        waitForAsyncAudit();
        final List<AuditLog> logs = auditLogRepository.findByActorId(adminUser.getId());
        assertThat(logs, is(not(empty())));

        final AuditLog log = logs.get(0);
        assertThat(log.getActor().getId(), is(equalTo(adminUser.getId())));
        assertThat(log.getMethod(), is(equalTo("GET")));
        assertThat(log.getRequestBody(), is(nullValue()));
        assertThat(log.getIpAddress(), is(notNullValue()));
        assertThat(log.getCreatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should persist audit log with request body when admin performs POST request")
    public void shouldPersistAuditLogWithBodyWhenAdminPerformsPostRequest() throws Exception {
        // Given: An admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid search request body
        final SortablePageInput input = aDefaultPageInput();

        // When: Admin performs a POST search request
        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input))
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);
        response.andExpect(status().is(SC_OK));

        // Then: An audit log entry should be persisted with request body
        waitForAsyncAudit();
        final List<AuditLog> logs = auditLogRepository.findByActorId(adminUser.getId());
        assertThat(logs, is(not(empty())));

        final AuditLog log = findAuditLogByMethod(logs, "POST");
        assertThat(log, is(notNullValue()));
        assertThat(log.getRequestBody(), is(notNullValue()));
        assertThat(log.getStatusCode(), is(equalTo((short) SC_OK)));
    }

    @Test
    @DisplayName("Should persist audit log for failed 403 request")
    public void shouldPersistAuditLogForForbiddenRequest() throws Exception {
        // Given: A regular user (no admin privileges)
        final User regularUser = aValidatedUser();

        // When: Regular user attempts to access admin endpoint
        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(aDefaultPageInput()))
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);
        response.andExpect(status().is(SC_FORBIDDEN));

        // Then: An audit log entry should be persisted with 403 status
        waitForAsyncAudit();
        final List<AuditLog> logs = auditLogRepository.findByActorId(regularUser.getId());
        assertThat(logs, is(not(empty())));

        final AuditLog log = logs.get(0);
        assertThat(log.getStatusCode(), is(equalTo((short) SC_FORBIDDEN)));
    }

    @Test
    @DisplayName("Should persist AuditEvent published via ApplicationEventPublisher")
    public void shouldPersistAuditEventPublishedViaEventPublisher() {
        // Given: An authenticated admin user
        final User actor = aValidatedUserWithRole(Role.ADMIN);

        // And: An AuditEvent published programmatically (non-interceptor path)
        final AuditEvent event = new AuditEvent(
            actor,
            "DELETE",
            "/v1/admin/api-keys/42",
            204,
            null,
            "10.0.0.5"
        );

        // When: Event is published
        eventPublisher.publishEvent(event);

        // Then: AuditLog should be persisted asynchronously
        waitForAsyncAudit();
        final List<AuditLog> logs = auditLogRepository.findByActorId(actor.getId());
        assertThat(logs, is(not(empty())));

        final AuditLog log = findAuditLogByMethod(logs, "DELETE");
        assertThat(log, is(notNullValue()));
        assertThat(log.getPath(), is(equalTo("/v1/admin/api-keys/42")));
        assertThat(log.getStatusCode(), is(equalTo((short) 204)));
        assertThat(log.getIpAddress(), is(equalTo("10.0.0.5")));
    }

    @Test
    @DisplayName("Should not persist audit log when unauthenticated request is made")
    public void shouldNotPersistAuditLogForUnauthenticatedRequest() throws Exception {
        // Given: No authentication provided
        final long countBefore = auditLogRepository.count();

        // When: Unauthenticated request to admin endpoint
        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(aDefaultPageInput()));

        final ResultActions response = mockMvc.perform(request);
        response.andExpect(status().is(SC_UNAUTHORIZED));

        // Then: No new audit log entries should be created
        waitForAsyncAudit();
        final long countAfter = auditLogRepository.count();
        assertThat(countAfter, is(equalTo(countBefore)));
    }

}
