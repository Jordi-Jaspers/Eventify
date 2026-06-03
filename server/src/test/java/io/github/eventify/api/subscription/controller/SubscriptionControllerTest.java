package io.github.eventify.api.subscription.controller;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.subscription.model.request.SubscribeRequest;
import io.github.eventify.api.subscription.model.response.SubscriptionResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.support.IntegrationTest;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Subscription Controller")
public class SubscriptionControllerTest extends IntegrationTest {

    // ========================= POST /subscription =========================

    @Test
    @DisplayName("Should create subscription successfully")
    public void createSubscriptionSuccess() throws Exception {
        // Given: An authenticated user with a watchlist
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // And: A valid subscribe request
        final SubscribeRequest request = aValidSubscribeRequest();

        // When: Creating subscription
        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain subscription details
        final String content = response.andReturn().getResponse().getContentAsString();
        final SubscriptionResponse subscriptionResponse = fromJson(content, SubscriptionResponse.class);

        assertThat(subscriptionResponse.getId(), is(notNullValue()));
        assertThat(subscriptionResponse.getWatchlistId(), is(watchlist.getId()));
        assertThat(subscriptionResponse.getTargetSeverities(), hasItems(Severity.CRITICAL));
        assertThat(subscriptionResponse.getAdapters(), hasItems(AdapterType.IN_APP));
        assertThat(subscriptionResponse.getCreatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should update existing subscription when subscribing again (upsert)")
    public void createSubscriptionUpsertSuccess() throws Exception {
        // Given: An authenticated user with a watchlist
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // And: User already has a subscription
        final SubscribeRequest firstRequest = aValidSubscribeRequest();
        mockMvc.perform(
            post(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(firstRequest))
        );

        // When: Subscribing again with different severities
        final SubscribeRequest updatedRequest = new SubscribeRequest();
        updatedRequest.setTargetSeverities(List.of(Severity.CRITICAL, Severity.WARNING));
        updatedRequest.setAdapters(List.of(AdapterType.IN_APP));

        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(updatedRequest));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED (upsert)
        response.andExpect(status().is(SC_CREATED));

        // And: Subscription should reflect updated severities
        final String content = response.andReturn().getResponse().getContentAsString();
        final SubscriptionResponse subscriptionResponse = fromJson(content, SubscriptionResponse.class);

        assertThat(subscriptionResponse.getTargetSeverities(), hasItems(Severity.CRITICAL, Severity.WARNING));
    }

    @Test
    @DisplayName("Should fail to subscribe when targetSeverities is empty")
    public void createSubscriptionFailsWhenTargetSeveritiesEmpty() throws Exception {
        // Given: An authenticated user with a watchlist
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // And: Request with empty targetSeverities
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of());
        request.setAdapters(List.of(AdapterType.IN_APP));

        // When: Creating subscription
        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail to subscribe when NO_DATA is in targetSeverities")
    public void createSubscriptionFailsWhenNoDataInSeverities() throws Exception {
        // Given: An authenticated user with a watchlist
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // And: Request with NO_DATA in targetSeverities
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of(Severity.CRITICAL, Severity.NO_DATA));
        request.setAdapters(List.of(AdapterType.IN_APP));

        // When: Creating subscription
        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail to subscribe when adapters does not include IN_APP")
    public void createSubscriptionFailsWhenAdaptersMissingInApp() throws Exception {
        // Given: An authenticated user with a watchlist
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // And: Request without IN_APP adapter
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of(Severity.CRITICAL));
        request.setAdapters(List.of(AdapterType.SLACK));

        // When: Creating subscription
        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail to subscribe to watchlist user does not own")
    public void createSubscriptionFailsWhenNotWatchlistOwner() throws Exception {
        // Given: Two authenticated users
        final User owner = aValidatedUser();
        final User otherUser = aValidatedUser();

        // And: Owner has a watchlist
        final Watchlist watchlist = aWatchlistForUser(owner, "Owner Watchlist");

        // When: Other user tries to subscribe
        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + otherUser.getAccessToken().getValue())
            .content(toJson(aValidSubscribeRequest()));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= GET /subscription =========================

    @Test
    @DisplayName("Should get subscription successfully")
    public void getSubscriptionSuccess() throws Exception {
        // Given: An authenticated user with a watchlist and subscription
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // And: User has a subscription
        mockMvc.perform(
            post(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(aValidSubscribeRequest()))
        );

        // When: Getting subscription
        final MockHttpServletRequestBuilder getRequest = get(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain subscription details
        final String content = response.andReturn().getResponse().getContentAsString();
        final SubscriptionResponse subscriptionResponse = fromJson(content, SubscriptionResponse.class);

        assertThat(subscriptionResponse.getId(), is(notNullValue()));
        assertThat(subscriptionResponse.getWatchlistId(), is(watchlist.getId()));
    }

    @Test
    @DisplayName("Should return 404 when no subscription exists")
    public void getSubscriptionNotFoundWhenNoneExists() throws Exception {
        // Given: An authenticated user with a watchlist but no subscription
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // When: Getting subscription
        final MockHttpServletRequestBuilder getRequest = get(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail to get subscription for watchlist user does not own")
    public void getSubscriptionFailsWhenNotWatchlistOwner() throws Exception {
        // Given: Two authenticated users
        final User owner = aValidatedUser();
        final User otherUser = aValidatedUser();

        // And: Owner has a watchlist
        final Watchlist watchlist = aWatchlistForUser(owner, "Owner Watchlist");

        // When: Other user tries to get subscription
        final MockHttpServletRequestBuilder getRequest = get(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .header(AUTHORIZATION, BEARER + otherUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= DELETE /subscription =========================

    @Test
    @DisplayName("Should delete subscription successfully")
    public void deleteSubscriptionSuccess() throws Exception {
        // Given: An authenticated user with a watchlist and subscription
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // And: User has a subscription
        mockMvc.perform(
            post(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(aValidSubscribeRequest()))
        );

        // When: Deleting subscription
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: Subscription should no longer be retrievable
        final MockHttpServletRequestBuilder getRequest = get(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        mockMvc.perform(getRequest).andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should return 404 when unsubscribing without an existing subscription")
    public void deleteSubscriptionFailsWhenNoneExists() throws Exception {
        // Given: An authenticated user with a watchlist but no subscription
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // When: Deleting subscription
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail to delete subscription for watchlist user does not own")
    public void deleteSubscriptionFailsWhenNotWatchlistOwner() throws Exception {
        // Given: Two authenticated users
        final User owner = aValidatedUser();
        final User otherUser = aValidatedUser();

        // And: Owner has a watchlist
        final Watchlist watchlist = aWatchlistForUser(owner, "Owner Watchlist");

        // When: Other user tries to delete subscription
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_WATCHLIST_SUBSCRIPTION_PATH, watchlist.getId())
            .header(AUTHORIZATION, BEARER + otherUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= FACTORY METHODS =========================

    private static SubscribeRequest aValidSubscribeRequest() {
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of(Severity.CRITICAL));
        request.setAdapters(List.of(AdapterType.IN_APP));
        return request;
    }
}
