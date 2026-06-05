package io.github.eventify.api.subscription.controller;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.subscription.model.request.CreateSubscriptionRequest;
import io.github.eventify.api.subscription.model.request.UpdateSubscriptionRequest;
import io.github.eventify.api.subscription.model.response.SubscriptionResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import tools.jackson.core.type.TypeReference;

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

    // ========================= POST /v1/subscriptions (personal create) =========================

    @Test
    @DisplayName("Should create personal subscription successfully")
    public void createPersonalSubscriptionSuccess() throws Exception {
        // Given: an authenticated user with a watchlist
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // When: posting a valid create subscription request
        final ResultActions response = mockMvc.perform(
            post(SUBSCRIPTIONS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(aValidCreateRequest(watchlist.getId())))
        );

        // Then: response is CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: response body contains subscription details
        final SubscriptionResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            SubscriptionResponse.class
        );
        assertThat(body.getId(), is(notNullValue()));
        assertThat(body.getWatchlistId(), is(watchlist.getId()));
        assertThat(body.getTargetSeverities(), hasItems(Severity.CRITICAL));
        assertThat(body.getAdapterConfigIds(), hasSize(1));
        assertThat(body.getCreatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should fail to create subscription when targetSeverities is empty")
    public void createPersonalSubscriptionFailsWhenTargetSeveritiesEmpty() throws Exception {
        // Given: an authenticated user with a watchlist
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // And: request with empty targetSeverities
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(watchlist.getId());
        request.setTargetSeverities(List.of());
        request.setAdapterConfigIds(List.of(1L));

        // When: posting the invalid request
        final ResultActions response = mockMvc.perform(
            post(SUBSCRIPTIONS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: response is BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail to create subscription when NO_DATA is in targetSeverities")
    public void createPersonalSubscriptionFailsWhenNoDataInSeverities() throws Exception {
        // Given: an authenticated user with a watchlist
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // And: request with NO_DATA severity
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(watchlist.getId());
        request.setTargetSeverities(List.of(Severity.CRITICAL, Severity.NO_DATA));
        request.setAdapterConfigIds(List.of(1L));

        // When: posting the invalid request
        final ResultActions response = mockMvc.perform(
            post(SUBSCRIPTIONS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: response is BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail to create subscription when adapterConfigIds is empty")
    public void createPersonalSubscriptionFailsWhenAdapterConfigIdsEmpty() throws Exception {
        // Given: an authenticated user with a watchlist
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        // And: request with empty adapterConfigIds
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(watchlist.getId());
        request.setTargetSeverities(List.of(Severity.CRITICAL));
        request.setAdapterConfigIds(List.of());

        // When: posting the invalid request
        final ResultActions response = mockMvc.perform(
            post(SUBSCRIPTIONS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: response is BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail to create subscription when not authenticated")
    public void createPersonalSubscriptionFailsWhenNotAuthenticated() throws Exception {
        // Given: no authentication header
        // When: posting without auth
        final ResultActions response = mockMvc.perform(
            post(SUBSCRIPTIONS_PATH)
                .contentType(APPLICATION_JSON)
                .content(toJson(aValidCreateRequest(1L)))
        );

        // Then: response is UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    // ========================= PUT /v1/subscriptions/{id} (personal update) =========================

    @Test
    @DisplayName("Should update personal subscription successfully")
    public void updatePersonalSubscriptionSuccess() throws Exception {
        // Given: an authenticated user with an existing subscription
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");
        final Long subscriptionId = createPersonalSubscriptionViaApi(user, watchlist.getId());

        // And: an update request with different severities
        final UpdateSubscriptionRequest updateRequest = new UpdateSubscriptionRequest();
        updateRequest.setTargetSeverities(List.of(Severity.CRITICAL, Severity.WARNING));
        updateRequest.setAdapterConfigIds(List.of(1L));

        // When: putting the update
        final ResultActions response = mockMvc.perform(
            put(SUBSCRIPTION_PATH, subscriptionId)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(updateRequest))
        );

        // Then: response is OK
        response.andExpect(status().is(SC_OK));

        // And: response body reflects updated severities
        final SubscriptionResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            SubscriptionResponse.class
        );
        assertThat(body.getTargetSeverities(), hasItems(Severity.CRITICAL, Severity.WARNING));
    }

    @Test
    @DisplayName("Should fail to update subscription belonging to another user")
    public void updatePersonalSubscriptionFailsWhenOwnershipMismatch() throws Exception {
        // Given: a subscription owned by another user
        final User owner = aValidatedUser();
        final User otherUser = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(owner, "Owner Watchlist");
        final Long subscriptionId = createPersonalSubscriptionViaApi(owner, watchlist.getId());

        // When: other user tries to update
        final ResultActions response = mockMvc.perform(
            put(SUBSCRIPTION_PATH, subscriptionId)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + otherUser.getAccessToken().getValue())
                .content(toJson(aValidUpdateRequest()))
        );

        // Then: response is FORBIDDEN or NOT_FOUND
        assertThat(
            response.andReturn().getResponse().getStatus(),
            anyOf(is(SC_FORBIDDEN), is(SC_NOT_FOUND))
        );
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent personal subscription")
    public void updatePersonalSubscriptionFailsWhenNotFound() throws Exception {
        // Given: an authenticated user
        final User user = aValidatedUser();

        // When: putting to a non-existent subscription ID
        final ResultActions response = mockMvc.perform(
            put(SUBSCRIPTION_PATH, 999999L)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(aValidUpdateRequest()))
        );

        // Then: response is NOT_FOUND or FORBIDDEN
        assertThat(
            response.andReturn().getResponse().getStatus(),
            anyOf(is(SC_NOT_FOUND), is(SC_FORBIDDEN))
        );
    }

    // ========================= DELETE /v1/subscriptions/{id} (personal delete) =========================

    @Test
    @DisplayName("Should delete personal subscription successfully")
    public void deletePersonalSubscriptionSuccess() throws Exception {
        // Given: an authenticated user with an existing subscription
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");
        final Long subscriptionId = createPersonalSubscriptionViaApi(user, watchlist.getId());

        // When: deleting the subscription
        final ResultActions response = mockMvc.perform(
            delete(SUBSCRIPTION_PATH, subscriptionId)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // Then: response is NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail to delete subscription belonging to another user")
    public void deletePersonalSubscriptionFailsWhenOwnershipMismatch() throws Exception {
        // Given: a subscription owned by another user
        final User owner = aValidatedUser();
        final User otherUser = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(owner, "Owner Watchlist");
        final Long subscriptionId = createPersonalSubscriptionViaApi(owner, watchlist.getId());

        // When: other user tries to delete
        final ResultActions response = mockMvc.perform(
            delete(SUBSCRIPTION_PATH, subscriptionId)
                .header(AUTHORIZATION, BEARER + otherUser.getAccessToken().getValue())
        );

        // Then: response is FORBIDDEN or NOT_FOUND
        assertThat(
            response.andReturn().getResponse().getStatus(),
            anyOf(is(SC_FORBIDDEN), is(SC_NOT_FOUND))
        );
    }

    // ========================= POST /v1/subscriptions/search (personal search) =========================

    @Test
    @DisplayName("Should return personal subscriptions for authenticated user")
    public void searchPersonalSubscriptionsSuccess() throws Exception {
        // Given: an authenticated user with a subscription
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");
        createPersonalSubscriptionViaApi(user, watchlist.getId());

        // When: searching personal subscriptions
        final ResultActions response = mockMvc.perform(
            post(SUBSCRIPTIONS_SEARCH_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(new SortablePageInput()))
        );

        // Then: response is OK
        response.andExpect(status().is(SC_OK));

        // And: result contains the user's subscription
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<SubscriptionResponse> page = objectMapper.readValue(
            content,
            new TypeReference<>() {}
        );
        assertThat(page.getTotalElements(), is(greaterThanOrEqualTo(1L)));
    }

    @Test
    @DisplayName("Should not return other users subscriptions in personal search")
    public void searchPersonalSubscriptionsReturnsOnlyCurrentUser() throws Exception {
        // Given: two users, each with their own subscriptions
        final User userA = aValidatedUser();
        final User userB = aValidatedUser();
        final Watchlist watchlistA = aWatchlistForUser(userA, "Watchlist A");
        final Watchlist watchlistB = aWatchlistForUser(userB, "Watchlist B");
        createPersonalSubscriptionViaApi(userA, watchlistA.getId());
        createPersonalSubscriptionViaApi(userB, watchlistB.getId());

        // When: userA searches their subscriptions
        final ResultActions response = mockMvc.perform(
            post(SUBSCRIPTIONS_SEARCH_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + userA.getAccessToken().getValue())
                .content(toJson(new SortablePageInput()))
        );

        // Then: response is OK
        response.andExpect(status().is(SC_OK));

        // And: result contains only userA's subscription
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<SubscriptionResponse> page = objectMapper.readValue(
            content,
            new TypeReference<>() {}
        );
        assertThat(page.getTotalElements(), is(1L));
    }

    // ========================= POST /v1/organization/{orgId}/subscriptions (org create) =========================

    @Test
    @DisplayName("Should create org subscription when user is org admin")
    public void createOrgSubscriptionSuccessWhenAdmin() throws Exception {
        // Given: an org owner with a watchlist
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");

        // When: owner creates an org subscription
        final ResultActions response = mockMvc.perform(
            post(ORGANIZATION_SUBSCRIPTIONS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(aValidCreateRequest(watchlist.getId())))
        );

        // Then: response is CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: body references the org
        final SubscriptionResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            SubscriptionResponse.class
        );
        assertThat(body.getId(), is(notNullValue()));
        assertThat(body.getWatchlistId(), is(watchlist.getId()));
    }

    @Test
    @DisplayName("Should fail to create org subscription when user is not org admin")
    public void createOrgSubscriptionFailsWhenNotAdmin() throws Exception {
        // Given: org with an owner, and a non-member user
        final User owner = aValidatedUser();
        final User nonMember = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");

        // When: non-member tries to create org subscription
        final ResultActions response = mockMvc.perform(
            post(ORGANIZATION_SUBSCRIPTIONS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
                .content(toJson(aValidCreateRequest(watchlist.getId())))
        );

        // Then: response is FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail to create org subscription when org member is not admin or owner")
    public void createOrgSubscriptionFailsWhenRegularMember() throws Exception {
        // Given: org with a regular member
        final User owner = aValidatedUser();
        final User member = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");

        // When: regular member tries to create org subscription
        final ResultActions response = mockMvc.perform(
            post(ORGANIZATION_SUBSCRIPTIONS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
                .content(toJson(aValidCreateRequest(watchlist.getId())))
        );

        // Then: response is FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= PUT /v1/organization/{orgId}/subscriptions/{id} (org update) =========================

    @Test
    @DisplayName("Should update org subscription when user is org owner")
    public void updateOrgSubscriptionSuccessWhenOwner() throws Exception {
        // Given: an org owner with an existing org subscription
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");
        final Long subscriptionId = createOrgSubscriptionViaApi(owner, org.getId(), watchlist.getId());

        // When: owner updates the org subscription
        final ResultActions response = mockMvc.perform(
            put(
                ORGANIZATION_SUBSCRIPTION_PATH
                    .replace("{orgId}", org.getId().toString())
                    .replace("{id}", subscriptionId.toString())
            )
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(aValidUpdateRequest()))
        );

        // Then: response is OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should fail to update org subscription when user is not admin")
    public void updateOrgSubscriptionFailsWhenNotAdmin() throws Exception {
        // Given: org with a regular member and an existing subscription
        final User owner = aValidatedUser();
        final User member = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");
        final Long subscriptionId = createOrgSubscriptionViaApi(owner, org.getId(), watchlist.getId());

        // When: regular member tries to update
        final ResultActions response = mockMvc.perform(
            put(
                ORGANIZATION_SUBSCRIPTION_PATH
                    .replace("{orgId}", org.getId().toString())
                    .replace("{id}", subscriptionId.toString())
            )
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
                .content(toJson(aValidUpdateRequest()))
        );

        // Then: response is FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= DELETE /v1/organization/{orgId}/subscriptions/{id} (org delete) =========================

    @Test
    @DisplayName("Should delete org subscription when user is org admin")
    public void deleteOrgSubscriptionSuccessWhenAdmin() throws Exception {
        // Given: an org owner with an existing org subscription
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");
        final Long subscriptionId = createOrgSubscriptionViaApi(owner, org.getId(), watchlist.getId());

        // When: owner deletes the org subscription
        final ResultActions response = mockMvc.perform(
            delete(
                ORGANIZATION_SUBSCRIPTION_PATH
                    .replace("{orgId}", org.getId().toString())
                    .replace("{id}", subscriptionId.toString())
            )
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
        );

        // Then: response is NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail to delete org subscription when user is not admin")
    public void deleteOrgSubscriptionFailsWhenNotAdmin() throws Exception {
        // Given: org with a regular member and an existing subscription
        final User owner = aValidatedUser();
        final User member = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");
        final Long subscriptionId = createOrgSubscriptionViaApi(owner, org.getId(), watchlist.getId());

        // When: regular member tries to delete
        final ResultActions response = mockMvc.perform(
            delete(
                ORGANIZATION_SUBSCRIPTION_PATH
                    .replace("{orgId}", org.getId().toString())
                    .replace("{id}", subscriptionId.toString())
            )
                .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
        );

        // Then: response is FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= POST /v1/organization/{orgId}/subscriptions/search (org search) =========================

    @Test
    @DisplayName("Should return org subscriptions for org admin")
    public void searchOrgSubscriptionsSuccessWhenAdmin() throws Exception {
        // Given: an org owner with an org subscription
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");
        createOrgSubscriptionViaApi(owner, org.getId(), watchlist.getId());

        // When: owner searches org subscriptions
        final ResultActions response = mockMvc.perform(
            post(ORGANIZATION_SUBSCRIPTIONS_SEARCH_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new SortablePageInput()))
        );

        // Then: response is OK with at least one result
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<SubscriptionResponse> page = objectMapper.readValue(
            content,
            new TypeReference<>() {}
        );
        assertThat(page.getTotalElements(), is(greaterThanOrEqualTo(1L)));
    }

    @Test
    @DisplayName("Should fail to search org subscriptions when user is not member")
    public void searchOrgSubscriptionsFailsWhenNotMember() throws Exception {
        // Given: org owned by someone else
        final User owner = aValidatedUser();
        final User stranger = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: stranger tries to search org subscriptions
        final ResultActions response = mockMvc.perform(
            post(ORGANIZATION_SUBSCRIPTIONS_SEARCH_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + stranger.getAccessToken().getValue())
                .content(toJson(new SortablePageInput()))
        );

        // Then: response is FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= HELPER METHODS =========================

    private Long createPersonalSubscriptionViaApi(final User user, final Long watchlistId) throws Exception {
        final MockHttpServletRequestBuilder request = post(SUBSCRIPTIONS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(aValidCreateRequest(watchlistId)));

        final String content = mockMvc.perform(request)
            .andExpect(status().is(SC_CREATED))
            .andReturn().getResponse().getContentAsString();

        return fromJson(content, SubscriptionResponse.class).getId();
    }

    private Long createOrgSubscriptionViaApi(final User user, final Long orgId, final Long watchlistId) throws Exception {
        final MockHttpServletRequestBuilder request =
            post(ORGANIZATION_SUBSCRIPTIONS_PATH.replace("{orgId}", orgId.toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(aValidCreateRequest(watchlistId)));

        final String content = mockMvc.perform(request)
            .andExpect(status().is(SC_CREATED))
            .andReturn().getResponse().getContentAsString();

        return fromJson(content, SubscriptionResponse.class).getId();
    }

    private static CreateSubscriptionRequest aValidCreateRequest(final Long watchlistId) {
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(watchlistId);
        request.setTargetSeverities(List.of(Severity.CRITICAL));
        request.setAdapterConfigIds(List.of(1L));
        return request;
    }

    private static UpdateSubscriptionRequest aValidUpdateRequest() {
        final UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setTargetSeverities(List.of(Severity.WARNING));
        request.setAdapterConfigIds(List.of(1L));
        return request;
    }
}
