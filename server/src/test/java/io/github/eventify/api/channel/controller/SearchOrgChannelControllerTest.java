package io.github.eventify.api.channel.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.channel.model.request.ChannelBatchRequest;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Integration Test - Search Organization Channels")
public class SearchOrgChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should search organization channels when user is owner")
    public void searchOrgChannelsSuccessWhenOwner() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has 3 channels
        for (int i = 1; i <= 3; i++) {
            final CreateChannelRequest createRequest = new CreateChannelRequest()
                .setName("Channel " + i)
                .setSlug("test.channel.1." + i);

            mockMvc.perform(
                post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                    .contentType(APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                    .content(toJson(createRequest))
            );
        }

        // When: Searching organization channels
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain 3 channels
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(3));
        assertThat(searchResponse.getTotalElements(), is(3L));
    }

    @Test
    @DisplayName("Should search organization channels when user is admin")
    public void searchOrgChannelsSuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        // And: Organization has channels
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Admin Viewable Channel")
            .setSlug("test.channel.2");

        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        // When: Searching organization channels as admin
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain channels
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("\"content\":"));
    }

    @Test
    @DisplayName("Should search organization channels when user is member")
    public void searchOrgChannelsSuccessWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Organization has channels
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Member Viewable Channel")
            .setSlug("test.channel.3");

        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        // When: Searching organization channels as member
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain channels
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("\"content\":"));
    }

    @Test
    @DisplayName("Should fail when non-member tries to search organization channels")
    public void searchOrgChannelsFailsWhenNonMember() throws Exception {
        // Given: An organization and a non-member user
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User nonMember = aValidatedUser();

        // When: Searching organization channels as non-member
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to search organization channels")
    public void searchOrgChannelsSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: Organization has channels
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Global Admin Viewable Channel")
            .setSlug("test.channel.4");

        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        // When: Searching organization channels as global admin
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain channels
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("\"content\":"));
    }

    @Test
    @DisplayName("Should return only organization channels not from other orgs")
    public void searchOrgChannelsReturnsOnlyOrgChannels() throws Exception {
        // Given: Two organizations
        final User owner1 = aValidatedUser();
        final Organization org1 = anOrganisationWithOwner(owner1);
        final User owner2 = aValidatedUser();
        final Organization org2 = anOrganisationWithOwner(owner2);

        // And: Org1 has 2 channels
        for (int i = 1; i <= 2; i++) {
            final CreateChannelRequest request = new CreateChannelRequest()
                .setName("Org1 Channel " + i)
                .setSlug("test.channel.5." + i);

            mockMvc.perform(
                post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org1.getId().toString()))
                    .contentType(APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + owner1.getAccessToken().getValue())
                    .content(toJson(request))
            );
        }

        // And: Org2 has 1 channel
        final CreateChannelRequest org2Request = new CreateChannelRequest()
            .setName("Org2 Channel")
            .setSlug("test.channel.6");

        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org2.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner2.getAccessToken().getValue())
                .content(toJson(org2Request))
        );

        // When: Searching Org1 channels
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org1.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner1.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should contain only Org1 channels
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(2));
        assertThat(searchResponse.getTotalElements(), is(2L));
    }

    @Test
    @DisplayName("Should not show deleted channels in search")
    public void searchDoesNotShowDeletedChannels() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has 2 channels
        final CreateChannelRequest createRequest1 = new CreateChannelRequest()
            .setName("Active Channel")
            .setSlug("test.channel.7");

        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest1))
        );

        final CreateChannelRequest createRequest2 = new CreateChannelRequest()
            .setName("Deleted Channel")
            .setSlug("test.channel.8");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest2))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final io.github.eventify.api.channel.model.response.ChannelDetailsResponse createdChannel =
            fromJson(createContent, io.github.eventify.api.channel.model.response.ChannelDetailsResponse.class);

        // And: Second channel is deleted
        mockMvc.perform(
            delete(
                ORGANIZATION_CHANNELS_PATH
                    .replace("{orgId}", org.getId().toString())
            )
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(createdChannel.getId()))))
        );

        // When: Searching channels
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Only active channel should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(1));
        assertThat(searchResponse.getTotalElements(), is(1L));
    }

    @Test
    @DisplayName("Should return empty list when organization has no channels")
    public void searchOrgChannelsReturnsEmptyWhenNoChannels() throws Exception {
        // Given: An organization with no channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Searching channels
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK with empty list
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), anyOf(nullValue(), is(empty())));
        assertThat(searchResponse.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user searches organization channels")
    public void searchOrgChannelsFailsWhenUnauthenticated() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Searching channels without authentication
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when searching channels for non-existent organization")
    public void searchOrgChannelsFailsWhenOrgNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Searching channels for non-existent organization
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", "99999")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be FORBIDDEN or NOT_FOUND
        assertThat(
            response.andReturn().getResponse().getStatus(),
            anyOf(is(SC_FORBIDDEN), is(SC_NOT_FOUND))
        );
    }

    @Test
    @DisplayName("Should return only active channels when filtering by status ACTIVE")
    public void searchByStatus_shouldReturnOnlyActiveChannels() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has 2 active channels
        final CreateChannelRequest activeRequest1 = new CreateChannelRequest()
            .setName("Active Channel 1")
            .setSlug("test.channel.9");
        final ResultActions createActiveResponse1 = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(activeRequest1))
        );
        createActiveResponse1.andExpect(status().is(SC_CREATED));

        final CreateChannelRequest activeRequest2 = new CreateChannelRequest()
            .setName("Active Channel 2")
            .setSlug("test.channel.10");
        final ResultActions createActiveResponse2 = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(activeRequest2))
        );
        createActiveResponse2.andExpect(status().is(SC_CREATED));

        // And: Organization has 1 paused channel
        final CreateChannelRequest pausedRequest = new CreateChannelRequest()
            .setName("Paused Channel")
            .setSlug("test.channel.11");
        final ResultActions createPausedResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(pausedRequest))
        );
        createPausedResponse.andExpect(status().is(SC_CREATED));

        final String pausedContent = createPausedResponse.andReturn().getResponse().getContentAsString();
        final io.github.eventify.api.channel.model.response.ChannelDetailsResponse pausedChannel =
            fromJson(pausedContent, io.github.eventify.api.channel.model.response.ChannelDetailsResponse.class);

        // And: Pause the third channel
        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(pausedChannel.getId()))))
        );

        // When: Searching channels with status filter ACTIVE
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName("status");
        statusFilter.setTextValue("ACTIVE");
        searchInput.getSearchInputs().add(statusFilter);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Only active channels should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(2));
        assertThat(searchResponse.getTotalElements(), is(2L));
    }

    @Test
    @DisplayName("Should return only paused channels when filtering by status PAUSED")
    public void searchByStatus_shouldReturnOnlyPausedChannels() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has 1 active channel
        final CreateChannelRequest activeRequest = new CreateChannelRequest()
            .setName("Active Channel")
            .setSlug("test.channel.12");
        final ResultActions createActiveResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(activeRequest))
        );
        createActiveResponse.andExpect(status().is(SC_CREATED));

        // And: Organization has 2 paused channels
        final CreateChannelRequest pausedRequest1 = new CreateChannelRequest()
            .setName("Paused Channel 1")
            .setSlug("test.channel.13");
        final ResultActions createPausedResponse1 = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(pausedRequest1))
        );
        createPausedResponse1.andExpect(status().is(SC_CREATED));

        final String pausedContent1 = createPausedResponse1.andReturn().getResponse().getContentAsString();
        final io.github.eventify.api.channel.model.response.ChannelDetailsResponse pausedChannel1 =
            fromJson(pausedContent1, io.github.eventify.api.channel.model.response.ChannelDetailsResponse.class);

        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(pausedChannel1.getId()))))
        );

        final CreateChannelRequest pausedRequest2 = new CreateChannelRequest()
            .setName("Paused Channel 2")
            .setSlug("test.channel.14");
        final ResultActions createPausedResponse2 = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(pausedRequest2))
        );
        createPausedResponse2.andExpect(status().is(SC_CREATED));

        final String pausedContent2 = createPausedResponse2.andReturn().getResponse().getContentAsString();
        final io.github.eventify.api.channel.model.response.ChannelDetailsResponse pausedChannel2 =
            fromJson(pausedContent2, io.github.eventify.api.channel.model.response.ChannelDetailsResponse.class);

        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(pausedChannel2.getId()))))
        );

        // When: Searching channels with status filter PAUSED
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName("status");
        statusFilter.setTextValue("PAUSED");
        searchInput.getSearchInputs().add(statusFilter);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_CHANNELS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Only paused channels should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(2));
        assertThat(searchResponse.getTotalElements(), is(2L));
    }
}
