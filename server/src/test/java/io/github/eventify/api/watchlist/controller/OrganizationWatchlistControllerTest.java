package io.github.eventify.api.watchlist.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistConfigurationRequest;
import io.github.eventify.api.watchlist.model.response.WatchlistDetailsResponse;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.github.jframe.exception.resource.ApiErrorResponseResource;

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

@DisplayName("Integration Test - Organization Watchlist Controller")
public class OrganizationWatchlistControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should create organization watchlist successfully")
    public void createOrganizationWatchlistSuccess() throws Exception {
        // Given: An authenticated user who is org OWNER
        final User user = aValidatedUser();
        final Organization org = anOrganisationWithOwner(user);

        // And: A valid create watchlist request
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Org Watchlist");

        // When: Creating org watchlist
        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder createRequest = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain watchlist details
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse watchlistResponse = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(watchlistResponse.getId(), is(notNullValue()));
        assertThat(watchlistResponse.getName(), is("Org Watchlist"));
    }

    @Test
    @DisplayName("Should create organization watchlist as admin successfully")
    public void createOrganizationWatchlistAsAdminSuccess() throws Exception {
        // Given: An org OWNER and an ADMIN member
        final User owner = aValidatedUser();
        final User admin = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // And: A valid create watchlist request
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Admin Watchlist");

        // When: Admin creates org watchlist
        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder createRequest = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain watchlist details
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse watchlistResponse = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(watchlistResponse.getId(), is(notNullValue()));
        assertThat(watchlistResponse.getName(), is("Admin Watchlist"));
    }

    @Test
    @DisplayName("Should fail when member creates organization watchlist")
    public void createOrganizationWatchlistAsMemberFails() throws Exception {
        // Given: An org OWNER and a MEMBER
        final User owner = aValidatedUser();
        final User member = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: A valid create watchlist request
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Member Watchlist");

        // When: Member attempts to create org watchlist
        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder createRequest = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should create organization watchlist with channels successfully")
    public void createOrganizationWatchlistWithChannelsSuccess() throws Exception {
        // Given: An authenticated user who is org OWNER
        final User user = aValidatedUser();
        final Organization org = anOrganisationWithOwner(user);

        // And: Organization has channels
        final Channel channel1 = aChannelForOrganisation(user, org, "Org Channel 1");
        final Channel channel2 = aChannelForOrganisation(user, org, "Org Channel 2");

        // And: Request with org channel IDs in configuration
        final WatchlistConfigurationRequest configRequest = new WatchlistConfigurationRequest()
            .setChannelIds(List.of(channel1.getId(), channel2.getId()));

        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Org Watchlist")
            .setConfiguration(configRequest);

        // When: Creating org watchlist
        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder createRequest = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain channels in configuration
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse watchlistResponse = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(watchlistResponse.getConfiguration(), is(notNullValue()));
        assertThat(watchlistResponse.getConfiguration().getChannelIds(), hasSize(2));
        assertThat(watchlistResponse.getConfiguration().getChannelIds(), containsInAnyOrder(channel1.getId(), channel2.getId()));
    }

    @Test
    @DisplayName("Should fail to create organization watchlist with personal channel")
    public void createOrganizationWatchlistWithPersonalChannelFails() throws Exception {
        // Given: An authenticated user who is org OWNER
        final User user = aValidatedUser();
        final Organization org = anOrganisationWithOwner(user);

        // And: User has a personal channel
        final Channel personalChannel = aChannelForUser(user, "Personal Channel");

        // When: Creating org watchlist with personal channel
        final WatchlistConfigurationRequest configRequest = new WatchlistConfigurationRequest()
            .setChannelIds(List.of(personalChannel.getId()));

        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Org Watchlist")
            .setConfiguration(configRequest);

        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder createRequest = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be NOT_FOUND or BAD_REQUEST
        assertThat(response.andReturn().getResponse().getStatus(), anyOf(is(SC_NOT_FOUND), is(SC_BAD_REQUEST)));
    }

    @Test
    @DisplayName("Should fail to create organization watchlist with other org channel")
    public void createOrganizationWatchlistWithOtherOrgChannelFails() throws Exception {
        // Given: Two organizations
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();
        final Organization org1 = anOrganisationWithOwner(user1);
        final Organization org2 = anOrganisationWithOwner(user2);

        // And: Org2 has a channel
        final Channel org2Channel = aChannelForOrganisation(user2, org2, "Org2 Channel");

        // When: User1 attempts to create org1 watchlist with org2 channel
        final WatchlistConfigurationRequest configRequest = new WatchlistConfigurationRequest()
            .setChannelIds(List.of(org2Channel.getId()));

        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Org Watchlist")
            .setConfiguration(configRequest);

        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org1.getId().toString());
        final MockHttpServletRequestBuilder createRequest = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be NOT_FOUND or BAD_REQUEST
        assertThat(response.andReturn().getResponse().getStatus(), anyOf(is(SC_NOT_FOUND), is(SC_BAD_REQUEST)));
    }

    @Test
    @DisplayName("Should fail to create organization watchlist with duplicate name")
    public void createOrganizationWatchlistDuplicateNameFails() throws Exception {
        // Given: An authenticated user who is org OWNER
        final User user = aValidatedUser();
        final Organization org = anOrganisationWithOwner(user);

        // And: Org already has watchlist with same name
        final CreateWatchlistRequest firstRequest = new CreateWatchlistRequest()
            .setName("Org Watchlist");

        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        mockMvc.perform(
            post(path)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(firstRequest))
        );

        // When: Creating another watchlist with same name
        final CreateWatchlistRequest duplicateRequest = new CreateWatchlistRequest()
            .setName("Org Watchlist");

        final MockHttpServletRequestBuilder createRequest = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(duplicateRequest));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention duplicate name
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), containsStringIgnoringCase("duplicate"));
    }

    @Test
    @DisplayName("Should search organization watchlists successfully")
    public void searchOrganizationWatchlistsSuccess() throws Exception {
        // Given: An authenticated user who is org MEMBER
        final User owner = aValidatedUser();
        final User member = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Org has 3 watchlists
        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        for (int i = 1; i <= 3; i++) {
            final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
                .setName("Org Watchlist " + i);

            mockMvc.perform(
                post(path)
                    .contentType(APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                    .content(toJson(createRequest))
            );
        }

        // When: Member searches watchlists
        final String searchPath = ORGANIZATION_WATCHLISTS_SEARCH_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder searchRequest = post(searchPath)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content("{}");

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain 3 watchlists
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(3));
        assertThat(searchResponse.getTotalElements(), is(3L));
    }

    @Test
    @DisplayName("Should get organization watchlist successfully")
    public void getOrganizationWatchlistSuccess() throws Exception {
        // Given: An authenticated user who is org MEMBER
        final User owner = aValidatedUser();
        final User member = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Org has a watchlist
        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("Org Watchlist");

        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final ResultActions createResponse = mockMvc.perform(
            post(path)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // When: Member gets watchlist by ID
        final String getPath = ORGANIZATION_WATCHLIST_PATH
            .replace("{orgId}", org.getId().toString())
            .replace("{id}", createdWatchlist.getId().toString());
        final MockHttpServletRequestBuilder getRequest = get(getPath)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain watchlist details
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse watchlistResponse = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(watchlistResponse.getId(), is(createdWatchlist.getId()));
        assertThat(watchlistResponse.getName(), is("Org Watchlist"));
    }

    @Test
    @DisplayName("Should update organization watchlist successfully")
    public void updateOrganizationWatchlistSuccess() throws Exception {
        // Given: An authenticated user who is org OWNER
        final User user = aValidatedUser();
        final Organization org = anOrganisationWithOwner(user);

        // And: Org has a watchlist
        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("Old Name");

        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final ResultActions createResponse = mockMvc.perform(
            post(path)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // When: Updating watchlist
        final UpdateWatchlistRequest updateRequest = new UpdateWatchlistRequest()
            .setName("New Name")
            .setDescription("New Description");

        final String updatePath = ORGANIZATION_WATCHLIST_PATH
            .replace("{orgId}", org.getId().toString())
            .replace("{id}", createdWatchlist.getId().toString());
        final MockHttpServletRequestBuilder updateRequestBuilder = put(updatePath)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Watchlist should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse updatedWatchlist = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(updatedWatchlist.getName(), is("New Name"));
        assertThat(updatedWatchlist.getDescription(), is("New Description"));
        assertThat(updatedWatchlist.getUpdatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should fail when member updates organization watchlist")
    public void updateOrganizationWatchlistAsMemberFails() throws Exception {
        // Given: An org OWNER and a MEMBER
        final User owner = aValidatedUser();
        final User member = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Org has a watchlist
        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("Org Watchlist");

        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final ResultActions createResponse = mockMvc.perform(
            post(path)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // When: Member attempts to update watchlist
        final UpdateWatchlistRequest updateRequest = new UpdateWatchlistRequest()
            .setName("Updated Name");

        final String updatePath = ORGANIZATION_WATCHLIST_PATH
            .replace("{orgId}", org.getId().toString())
            .replace("{id}", createdWatchlist.getId().toString());
        final MockHttpServletRequestBuilder updateRequestBuilder = put(updatePath)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should delete organization watchlist successfully")
    public void deleteOrganizationWatchlistSuccess() throws Exception {
        // Given: An authenticated user who is org OWNER
        final User user = aValidatedUser();
        final Organization org = anOrganisationWithOwner(user);

        // And: Org has a watchlist
        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("Watchlist to Delete");

        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final ResultActions createResponse = mockMvc.perform(
            post(path)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // When: Deleting the watchlist
        final String deletePath = ORGANIZATION_WATCHLIST_PATH
            .replace("{orgId}", org.getId().toString())
            .replace("{id}", createdWatchlist.getId().toString());
        final MockHttpServletRequestBuilder deleteRequest = delete(deletePath)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NO_CONTENT or OK
        assertThat(response.andReturn().getResponse().getStatus(), anyOf(is(SC_NO_CONTENT), is(SC_OK)));

        // And: Watchlist should not be retrievable
        final MockHttpServletRequestBuilder getRequest = get(deletePath)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions getResponse = mockMvc.perform(getRequest);
        getResponse.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when member deletes organization watchlist")
    public void deleteOrganizationWatchlistAsMemberFails() throws Exception {
        // Given: An org OWNER and a MEMBER
        final User owner = aValidatedUser();
        final User member = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Org has a watchlist
        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("Org Watchlist");

        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final ResultActions createResponse = mockMvc.perform(
            post(path)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // When: Member attempts to delete watchlist
        final String deletePath = ORGANIZATION_WATCHLIST_PATH
            .replace("{orgId}", org.getId().toString())
            .replace("{id}", createdWatchlist.getId().toString());
        final MockHttpServletRequestBuilder deleteRequest = delete(deletePath)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to create organization watchlist")
    public void globalAdminCanCreateOrgWatchlist() throws Exception {
        // Given: An organization with OWNER
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: A global admin (not org member)
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid create watchlist request
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Global Admin Watchlist");

        // When: Global admin creates org watchlist
        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder createRequest = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain watchlist details
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse watchlistResponse = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(watchlistResponse.getId(), is(notNullValue()));
        assertThat(watchlistResponse.getName(), is("Global Admin Watchlist"));
    }

    @Test
    @DisplayName("Should allow global admin to update organization watchlist")
    public void globalAdminCanUpdateOrgWatchlist() throws Exception {
        // Given: An organization with OWNER and a watchlist
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("Old Name");

        final String path = ORGANIZATION_WATCHLISTS_PATH.replace("{orgId}", org.getId().toString());
        final ResultActions createResponse = mockMvc.perform(
            post(path)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // And: A global admin (not org member)
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // When: Global admin updates watchlist
        final UpdateWatchlistRequest updateRequest = new UpdateWatchlistRequest()
            .setName("Updated by Global Admin");

        final String updatePath = ORGANIZATION_WATCHLIST_PATH
            .replace("{orgId}", org.getId().toString())
            .replace("{id}", createdWatchlist.getId().toString());
        final MockHttpServletRequestBuilder updateRequestBuilder = put(updatePath)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Watchlist should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse updatedWatchlist = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(updatedWatchlist.getName(), is("Updated by Global Admin"));
    }
}
