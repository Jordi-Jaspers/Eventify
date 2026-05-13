package io.github.eventify.api.notification.controller;

import io.github.eventify.api.notification.model.NotificationBroadcast;
import io.github.eventify.api.notification.model.response.RecipientResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import tools.jackson.core.type.TypeReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ADMIN_BROADCAST_RECIPIENTS_SEARCH_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Integration Test - Broadcast Recipients Controller")
public class BroadcastRecipientsControllerTest extends IntegrationTest {

    // ========================= POST /broadcasts/{id}/recipients/search — happy path =========================

    @Test
    @DisplayName("Should return paginated recipients for a broadcast when admin requests")
    public void getRecipientsSuccess() throws Exception {
        // Given: A broadcast with recipients
        final User recipient1 = aValidatedUser();
        final User recipient2 = aValidatedUser();
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "Test Broadcast", "ALL_USERS");
        aNotificationForUserWithBroadcast(recipient1, broadcast);
        aNotificationForUserWithBroadcast(recipient2, broadcast);

        // When: Admin requests recipients for the broadcast
        final SortablePageInput input = aDefaultPageInput();
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain paginated recipients
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<RecipientResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getTotalElements(), is(2L));
        assertThat(pageResource.getContent(), hasSize(2));
    }

    @Test
    @DisplayName("Should return recipient with userId, email and name fields populated")
    public void getRecipientsReturnsCorrectFields() throws Exception {
        // Given: A broadcast with one recipient
        final User recipient = aValidatedUser();
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "Field Test Broadcast", "ALL_USERS");
        aNotificationForUserWithBroadcast(recipient, broadcast);

        // When: Admin requests recipients
        final SortablePageInput input = aDefaultPageInput();
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Recipient fields should be populated correctly
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<RecipientResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getContent(), hasSize(1));

        final RecipientResponse recipientResponse = pageResource.getContent().get(0);
        assertThat(recipientResponse.getUserId(), is(recipient.getId()));
        assertThat(recipientResponse.getEmail(), is(recipient.getEmail()));
        assertThat(recipientResponse.getName(), is(notNullValue()));
    }

    // ========================= POST /broadcasts/{id}/recipients/search — search =========================

    @Test
    @DisplayName("Should filter recipients by email when search input provided")
    public void getRecipientsFiltersByEmail() throws Exception {
        // Given: A broadcast with two recipients with distinct emails
        final User matchingRecipient = aValidatedUser();
        final User otherRecipient = aValidatedUser();
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "Email Filter Broadcast", "ALL_USERS");
        aNotificationForUserWithBroadcast(matchingRecipient, broadcast);
        aNotificationForUserWithBroadcast(otherRecipient, broadcast);

        // When: Admin searches by partial email of the matching recipient
        final String emailFragment = matchingRecipient.getEmail().substring(0, 5);
        final SortablePageInput input = aSearchPageInput("search", emailFragment);
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Only the matching recipient should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<RecipientResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getContent(), is(not(empty())));
        pageResource.getContent().forEach(
            r -> assertThat(r.getEmail().toLowerCase(), containsString(emailFragment.toLowerCase()))
        );
    }

    @Test
    @DisplayName("Should filter recipients by name when search input provided")
    public void getRecipientsFiltersByName() throws Exception {
        // Given: A broadcast with recipients
        final User recipient = aValidatedUser();
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "Name Filter Broadcast", "ALL_USERS");
        aNotificationForUserWithBroadcast(recipient, broadcast);

        // When: Admin searches by first name (FIRST_NAME = "John")
        final SortablePageInput input = aSearchPageInput("search", FIRST_NAME);
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Recipients matching the name should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<RecipientResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getContent(), is(not(empty())));
        pageResource.getContent().forEach(
            r -> assertThat(r.getName().toLowerCase(), containsString(FIRST_NAME.toLowerCase()))
        );
    }

    @Test
    @DisplayName("Should return all recipients when search input is empty")
    public void getRecipientsReturnsAllWhenSearchEmpty() throws Exception {
        // Given: A broadcast with two recipients
        final User recipient1 = aValidatedUser();
        final User recipient2 = aValidatedUser();
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "Empty Search Broadcast", "ALL_USERS");
        aNotificationForUserWithBroadcast(recipient1, broadcast);
        aNotificationForUserWithBroadcast(recipient2, broadcast);

        // When: Admin requests recipients with empty search
        final SortablePageInput input = aDefaultPageInput();
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: All recipients should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<RecipientResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getTotalElements(), is(2L));
    }

    @Test
    @DisplayName("Should return empty content when search term matches no recipients")
    public void getRecipientsReturnsEmptyWhenSearchMatchesNone() throws Exception {
        // Given: A broadcast with a recipient
        final User recipient = aValidatedUser();
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "No Match Broadcast", "ALL_USERS");
        aNotificationForUserWithBroadcast(recipient, broadcast);

        // When: Admin searches with a term that matches no recipient
        final SortablePageInput input = aSearchPageInput("search", "zzznomatch999xyz");
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Content should be empty
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<RecipientResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getTotalElements(), is(0L));
    }

    // ========================= POST /broadcasts/{id}/recipients/search — pagination =========================

    @Test
    @DisplayName("Should respect pageSize and pageNumber pagination params")
    public void getRecipientsPaginationWorks() throws Exception {
        // Given: A broadcast with 3 recipients
        final User recipient1 = aValidatedUser();
        final User recipient2 = aValidatedUser();
        final User recipient3 = aValidatedUser();
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "Pagination Broadcast", "ALL_USERS");
        aNotificationForUserWithBroadcast(recipient1, broadcast);
        aNotificationForUserWithBroadcast(recipient2, broadcast);
        aNotificationForUserWithBroadcast(recipient3, broadcast);

        // When: Admin requests with pageSize=2 and pageNumber=0
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(2);
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Only 2 recipients should be returned with correct pagination metadata
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<RecipientResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getContent(), hasSize(2));
        assertThat(pageResource.getTotalElements(), is(3L));
    }

    @Test
    @DisplayName("Should return empty content when pageNumber is beyond total results")
    public void getRecipientsReturnsEmptyWhenPageBeyondTotal() throws Exception {
        // Given: A broadcast with 2 recipients
        final User recipient1 = aValidatedUser();
        final User recipient2 = aValidatedUser();
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "Offset Broadcast", "ALL_USERS");
        aNotificationForUserWithBroadcast(recipient1, broadcast);
        aNotificationForUserWithBroadcast(recipient2, broadcast);

        // When: Admin requests with pageNumber beyond total
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(100);
        input.setPageSize(20);
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Content should be empty but totalElements should reflect actual count
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<RecipientResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getTotalElements(), is(2L));
    }

    // ========================= POST /broadcasts/{id}/recipients/search — edge cases =========================

    @Test
    @DisplayName("Should return empty list when broadcast has no recipients")
    public void getRecipientsReturnsEmptyForBroadcastWithNoRecipients() throws Exception {
        // Given: A broadcast with no associated notifications
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "Empty Broadcast", "ALL_USERS");

        // When: Admin requests recipients
        final SortablePageInput input = aDefaultPageInput();
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Content should be empty
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<RecipientResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getTotalElements(), is(0L));
    }

    // ========================= POST /broadcasts/{id}/recipients/search — 404 =========================

    @Test
    @DisplayName("Should return 404 when broadcast does not exist")
    public void getRecipientsReturns404ForNonExistentBroadcast() throws Exception {
        // Given: A non-existent broadcast ID
        final long nonExistentId = 999999L;

        // When: Admin requests recipients for non-existent broadcast
        final SortablePageInput input = aDefaultPageInput();
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(nonExistentId))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be NOT FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    // ========================= POST /broadcasts/{id}/recipients/search — security =========================

    @Test
    @DisplayName("Should return 403 when user without MANAGE_USERS authority requests recipients")
    public void getRecipientsFailsWhenNotAdmin() throws Exception {
        // Given: A regular user and an existing broadcast
        final User regularUser = aValidatedUser();
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "Security Test Broadcast", "ALL_USERS");

        // When: Regular user attempts to get recipients
        final SortablePageInput input = aDefaultPageInput();
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user requests recipients")
    public void getRecipientsFailsWhenUnauthenticated() throws Exception {
        // Given: An existing broadcast and no authentication
        final NotificationBroadcast broadcast = aBroadcastForAdmin(admin, "Auth Test Broadcast", "ALL_USERS");

        // When: Unauthenticated request
        final SortablePageInput input = aDefaultPageInput();
        final MockHttpServletRequestBuilder request = post(buildRecipientsSearchPath(broadcast.getId()))
            .contentType(APPLICATION_JSON)
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    // ========================= HELPER METHODS =========================

    private static String buildRecipientsSearchPath(final long broadcastId) {
        return ADMIN_BROADCAST_RECIPIENTS_SEARCH_PATH.replace("{id}", String.valueOf(broadcastId));
    }
}
