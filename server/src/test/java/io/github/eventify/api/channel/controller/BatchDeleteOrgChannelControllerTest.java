package io.github.eventify.api.channel.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.channel.model.request.ChannelBatchRequest;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Batch Delete Organization Channel")
public class BatchDeleteOrgChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should batch delete channels when user is owner")
    public void batchDeleteOrgChannelsSuccessWhenOwner() throws Exception {
        // Given: An organization owner with two channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel1 = createOrgChannel(owner, org, "Channel to Delete 1", "batch.org.delete.1");
        final ChannelDetailsResponse channel2 = createOrgChannel(owner, org, "Channel to Delete 2", "batch.org.delete.2");

        // When: Deleting both channels in a batch
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel1.getId(), channel2.getId()));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should batch delete channels when user is org admin")
    public void batchDeleteOrgChannelsSuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Channel to Delete", "batch.org.delete.admin");

        // When: Deleting as admin
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail when member tries to batch delete organization channels")
    public void batchDeleteOrgChannelsFailsWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Protected Channel", "batch.org.delete.member");

        // When: Deleting as member
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when non-member tries to batch delete organization channels")
    public void batchDeleteOrgChannelsFailsWhenNonMember() throws Exception {
        // Given: An organization and a non-member user
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User nonMember = aValidatedUser();

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Protected Channel", "batch.org.delete.nonmember");

        // When: Deleting as non-member
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to batch delete organization channels")
    public void batchDeleteOrgChannelsSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Channel to Delete", "batch.org.delete.globaladmin");

        // When: Deleting as global admin
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail when batch deleting non-existent channels")
    public void batchDeleteOrgChannelsFailsWhenNotFound() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Deleting non-existent channels
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(99999L));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when batch deleting already deleted channels")
    public void batchDeleteOrgChannelsFailsWhenAlreadyDeleted() throws Exception {
        // Given: An organization owner with a deleted channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Deleted Channel", "batch.org.delete.already.deleted");

        // And: Channel is already deleted
        mockMvc.perform(
            delete(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))))
        );

        // When: Attempting to delete again
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when batch deleting channels from different organization")
    public void batchDeleteOrgChannelsFailsWhenDifferentOrg() throws Exception {
        // Given: Two organizations with different owners
        final User owner1 = aValidatedUser();
        final Organization org1 = anOrganisationWithOwner(owner1);
        final User owner2 = aValidatedUser();
        final Organization org2 = anOrganisationWithOwner(owner2);

        // And: Org1 has a channel
        final ChannelDetailsResponse channel = createOrgChannel(owner1, org1, "Org1 Channel", "batch.org.delete.diff.org");

        // When: Owner2 tries to delete Org1's channel using Org2 path
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org2.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner2.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NOT_FOUND (don't expose existence)
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user batch deletes organization channels")
    public void batchDeleteOrgChannelsFailsWhenUnauthenticated() throws Exception {
        // Given: An organization with a channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Test Channel", "batch.org.delete.unauth");

        // When: Deleting without authentication
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when batch request has empty channelIds")
    public void batchDeleteOrgChannelsFailsWhenEmptyIds() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Sending batch request with empty channelIds
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of());

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when batch request body is null")
    public void batchDeleteOrgChannelsFailsWhenNullBody() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Sending batch delete with no body
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail entire batch when mix of valid and invalid channel IDs")
    public void batchDeleteOrgChannelsFailsWhenMixedValidInvalidIds() throws Exception {
        // Given: An organization owner with one valid channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Valid Channel", "batch.org.delete.mixed");

        // When: Deleting with a mix of valid and non-existent IDs
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId(), 99999L));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when organization not found")
    public void batchDeleteOrgChannelsFailsWhenOrgNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Deleting channels for non-existent organization
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(1L));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", "99999")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN or NOT_FOUND
        assertThat(
            response.andReturn().getResponse().getStatus(),
            anyOf(is(SC_FORBIDDEN), is(SC_NOT_FOUND))
        );
    }

    @Test
    @DisplayName("Should not delete valid channel when batch fails due to mixed valid and invalid IDs")
    public void batchDeleteOrgChannelsFailsWhenMixedValidInvalidIds_stateCheck() throws Exception {
        // Given: An organization owner with one valid channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Valid Channel State Check", "batch.org.delete.mixed.state");

        // When: Deleting with a mix of valid and non-existent IDs
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId(), 99999L));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_NOT_FOUND));

        // And: Valid channel should NOT be deleted (still ACTIVE)
        assertOrgChannelStatus(owner, org, channel.getId(), "ACTIVE");
    }

    @Test
    @DisplayName("Should fail entire batch delete when mix of active and already-deleted org channel IDs")
    public void batchDeleteOrgChannelsFailsWhenMixedValidAndAlreadyDeleted() throws Exception {
        // Given: An organization owner with two channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse remainingChannel = createOrgChannel(
            owner,
            org,
            "Remaining Org Channel",
            "batch.org.delete.mixed.remaining"
        );
        final ChannelDetailsResponse deletedChannel = createOrgChannel(
            owner,
            org,
            "Pre-Deleted Org Channel",
            "batch.org.delete.mixed.predeleted"
        );

        // And: One channel is already deleted
        mockMvc.perform(
            delete(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(deletedChannel.getId()))))
        );

        // When: Batch deleting remaining channel together with already-deleted channel
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(remainingChannel.getId(), deletedChannel.getId()));

        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_NOT_FOUND));

        // And: Remaining channel should NOT be deleted (still ACTIVE)
        assertOrgChannelStatus(owner, org, remainingChannel.getId(), "ACTIVE");
    }

    // ========================= PRIVATE HELPERS =========================

    private ChannelDetailsResponse createOrgChannel(final User user, final Organization org,
        final String name, final String slug) throws Exception {
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName(name)
            .setSlug(slug);

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        final String content = createResponse.andReturn().getResponse().getContentAsString();
        return fromJson(content, ChannelDetailsResponse.class);
    }

    private void assertOrgChannelStatus(final User user, final Organization org,
        final Long channelId, final String expectedStatus) throws Exception {
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", channelId.toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions getResponse = mockMvc.perform(getRequest);
        final String content = getResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channel.getStatus(), is(expectedStatus));
    }
}
