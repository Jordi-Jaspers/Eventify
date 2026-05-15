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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Batch Pause/Resume Organization Channel")
public class BatchPauseResumeOrgChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should batch pause channels when user is owner")
    public void batchPauseOrgChannelsSuccessWhenOwner() throws Exception {
        // Given: An organization owner with two active channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel1 = createOrgChannel(owner, org, "Active Channel 1", "batch.org.pause.1");
        final ChannelDetailsResponse channel2 = createOrgChannel(owner, org, "Active Channel 2", "batch.org.pause.2");

        // When: Pausing both channels in a batch
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel1.getId(), channel2.getId()));

        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: Both channels should be PAUSED
        assertOrgChannelStatus(owner, org, channel1.getId(), "PAUSED");
        assertOrgChannelStatus(owner, org, channel2.getId(), "PAUSED");
    }

    @Test
    @DisplayName("Should batch pause channels when user is org admin")
    public void batchPauseOrgChannelsSuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Active Channel", "batch.org.pause.admin");

        // When: Pausing as admin
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail when member tries to batch pause organization channels")
    public void batchPauseOrgChannelsFailsWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Protected Channel", "batch.org.pause.member");

        // When: Pausing as member
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when non-member tries to batch pause organization channels")
    public void batchPauseOrgChannelsFailsWhenNonMember() throws Exception {
        // Given: An organization and a non-member user
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User nonMember = aValidatedUser();

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Protected Channel", "batch.org.pause.nonmember");

        // When: Pausing as non-member
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to batch pause organization channels")
    public void batchPauseOrgChannelsSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Active Channel", "batch.org.pause.globaladmin");

        // When: Pausing as global admin
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should be idempotent when batch pausing already paused channels")
    public void batchPauseOrgChannelsIdempotentWhenAlreadyPaused() throws Exception {
        // Given: An organization owner with a paused channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Paused Channel", "batch.org.pause.idempotent");

        // And: Channel is already paused
        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))))
        );

        // When: Pausing again
        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be NO_CONTENT (idempotent)
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should batch resume channels when user is owner")
    public void batchResumeOrgChannelsSuccessWhenOwner() throws Exception {
        // Given: An organization owner with two paused channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel1 = createOrgChannel(owner, org, "Paused Channel 1", "batch.org.resume.1");
        final ChannelDetailsResponse channel2 = createOrgChannel(owner, org, "Paused Channel 2", "batch.org.resume.2");

        // And: Both channels are paused
        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel1.getId(), channel2.getId()))))
        );

        // When: Resuming both channels in a batch
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel1.getId(), channel2.getId()));

        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNELS_RESUME_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: Both channels should be ACTIVE
        assertOrgChannelStatus(owner, org, channel1.getId(), "ACTIVE");
        assertOrgChannelStatus(owner, org, channel2.getId(), "ACTIVE");
    }

    @Test
    @DisplayName("Should batch resume channels when user is org admin")
    public void batchResumeOrgChannelsSuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Paused Channel", "batch.org.resume.admin");

        // And: Channel is paused
        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))))
        );

        // When: Resuming as admin
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNELS_RESUME_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail when member tries to batch resume organization channels")
    public void batchResumeOrgChannelsFailsWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Paused Channel", "batch.org.resume.member");

        // When: Resuming as member
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNELS_RESUME_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to batch resume organization channels")
    public void batchResumeOrgChannelsSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Paused Channel", "batch.org.resume.globaladmin");

        // And: Channel is paused
        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))))
        );

        // When: Resuming as global admin
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNELS_RESUME_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should be idempotent when batch resuming already active channels")
    public void batchResumeOrgChannelsIdempotentWhenAlreadyActive() throws Exception {
        // Given: An organization owner with an active channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Active Channel", "batch.org.resume.idempotent");

        // When: Resuming an already active channel
        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNELS_RESUME_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be NO_CONTENT (idempotent)
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail when batch pausing non-existent channels")
    public void batchPauseOrgChannelsFailsWhenNotFound() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Pausing non-existent channels
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(99999L));

        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when batch resuming non-existent channels")
    public void batchResumeOrgChannelsFailsWhenNotFound() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Resuming non-existent channels
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(99999L));

        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNELS_RESUME_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user batch pauses channels")
    public void batchPauseOrgChannelsFailsWhenUnauthenticated() throws Exception {
        // Given: An organization with a channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Test Channel", "batch.org.pause.unauth");

        // When: Pausing without authentication
        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user batch resumes channels")
    public void batchResumeOrgChannelsFailsWhenUnauthenticated() throws Exception {
        // Given: An organization with a channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Test Channel", "batch.org.resume.unauth");

        // When: Resuming without authentication
        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNELS_RESUME_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when batch request has empty channelIds")
    public void batchPauseOrgChannelsFailsWhenEmptyIds() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Sending batch request with empty channelIds
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of());

        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail entire batch when mix of valid and invalid channel IDs")
    public void batchPauseOrgChannelsFailsWhenMixedValidInvalidIds() throws Exception {
        // Given: An organization owner with one valid channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Valid Channel", "batch.org.pause.mixed");

        // When: Pausing with a mix of valid and non-existent IDs
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId(), 99999L));

        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNELS_PAUSE_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_NOT_FOUND));

        // And: Valid channel should NOT be paused
        assertOrgChannelStatus(owner, org, channel.getId(), "ACTIVE");
    }

    @Test
    @DisplayName("Should fail entire batch resume when mix of valid and non-existent org channel IDs")
    public void batchResumeOrgChannelsFailsWhenMixedValidInvalidIds() throws Exception {
        // Given: An organization owner with one active channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final ChannelDetailsResponse channel = createOrgChannel(owner, org, "Active Channel Resume Mixed", "batch.org.resume.mixed");

        // When: Resuming with a mix of valid and non-existent IDs
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId(), 99999L));

        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNELS_RESUME_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_NOT_FOUND));

        // And: Valid channel should NOT be mutated (still ACTIVE)
        assertOrgChannelStatus(owner, org, channel.getId(), "ACTIVE");
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
