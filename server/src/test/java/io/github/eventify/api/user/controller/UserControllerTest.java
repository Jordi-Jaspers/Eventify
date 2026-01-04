package io.github.eventify.api.user.controller;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.request.UpdateUserDetailsRequest;
import io.github.eventify.api.user.model.response.UserDetailsResponse;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - User Controller")
public class UserControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return user details for authenticated user")
    public void shouldReturnUserDetailsForAuthenticatedUser() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Requesting user details
        final MockHttpServletRequestBuilder request = get(USER_DETAILS)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain the user details
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);

        assertThat(userDetails.getId(), is(user.getId()));
        assertThat(userDetails.getEmail(), is(user.getEmail()));
        assertThat(userDetails.getFirstName(), is(user.getFirstName()));
        assertThat(userDetails.getLastName(), is(user.getLastName()));
        assertThat(userDetails.getPermissions(), not(empty()));
    }

    @Test
    @DisplayName("Should update user details successfully")
    public void shouldUpdateUserDetailsSuccessfully() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Update details request
        final UpdateUserDetailsRequest request = anUpdateUserDetailsRequest();

        // When: Updating user details
        final MockHttpServletRequestBuilder updateRequest = post(USER_DETAILS)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain updated user details
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);

        assertThat(userDetails.getId(), is(user.getId()));
        assertThat(userDetails.getFirstName(), is(request.getFirstName()));
        assertThat(userDetails.getLastName(), is(request.getLastName()));
    }

    @Test
    @DisplayName("Should return empty organizations list when user has no organizations")
    public void shouldReturnEmptyOrganizationsListWhenUserHasNoOrganizations() throws Exception {
        // Given: An authenticated user with no organization memberships
        final User user = aValidatedUser();

        // When: Requesting user details
        final MockHttpServletRequestBuilder request = get(USER_DETAILS)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain empty organizations list
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);

        assertThat(userDetails.getId(), is(user.getId()));
        assertThat(userDetails.getOrganizations(), is(notNullValue()));
        assertThat(userDetails.getOrganizations(), is(empty()));
    }

    @Test
    @DisplayName("Should return user details with organizations when user has memberships")
    public void shouldReturnUserDetailsWithOrganizationsWhenUserHasMemberships() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User is owner of one organization
        final io.github.eventify.api.organization.model.Organization org1 = createOrganization(user);

        // And: User is admin in another organization
        final User otherOwner = aValidatedUser();
        final io.github.eventify.api.organization.model.Organization org2 = createOrganization(otherOwner);
        addMemberToOrganization(org2, user, io.github.eventify.api.organization.model.OrganizationalRole.ADMIN);

        // And: User is member in a third organization
        final User anotherOwner = aValidatedUser();
        final io.github.eventify.api.organization.model.Organization org3 = createOrganization(anotherOwner);
        addMemberToOrganization(org3, user, io.github.eventify.api.organization.model.OrganizationalRole.MEMBER);

        // When: Requesting user details
        final MockHttpServletRequestBuilder request = get(USER_DETAILS)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain all organization memberships
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);

        assertThat(userDetails.getId(), is(user.getId()));
        assertThat(userDetails.getOrganizations(), is(notNullValue()));
        assertThat(userDetails.getOrganizations(), hasSize(3));

        // And: Organization details should be correct
        final io.github.eventify.api.organization.model.response.UserOrganizationResponse org1Response =
            userDetails.getOrganizations().stream()
                .filter(org -> org.getOrganizationId().equals(org1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(org1Response.getOrganizationName(), is(org1.getName()));
        assertThat(org1Response.getOrganizationSlug(), is(org1.getSlug()));
        assertThat(org1Response.getRole(), is(io.github.eventify.api.organization.model.OrganizationalRole.OWNER));
        assertThat(org1Response.getJoinedAt(), is(notNullValue()));

        final io.github.eventify.api.organization.model.response.UserOrganizationResponse org2Response =
            userDetails.getOrganizations().stream()
                .filter(org -> org.getOrganizationId().equals(org2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(org2Response.getOrganizationName(), is(org2.getName()));
        assertThat(org2Response.getOrganizationSlug(), is(org2.getSlug()));
        assertThat(org2Response.getRole(), is(io.github.eventify.api.organization.model.OrganizationalRole.ADMIN));
        assertThat(org2Response.getJoinedAt(), is(notNullValue()));

        final io.github.eventify.api.organization.model.response.UserOrganizationResponse org3Response =
            userDetails.getOrganizations().stream()
                .filter(org -> org.getOrganizationId().equals(org3.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(org3Response.getOrganizationName(), is(org3.getName()));
        assertThat(org3Response.getOrganizationSlug(), is(org3.getSlug()));
        assertThat(org3Response.getRole(), is(io.github.eventify.api.organization.model.OrganizationalRole.MEMBER));
        assertThat(org3Response.getJoinedAt(), is(notNullValue()));
    }

}
