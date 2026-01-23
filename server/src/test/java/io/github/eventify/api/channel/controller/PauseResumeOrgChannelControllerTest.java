package io.github.eventify.api.channel.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Integration Test - Pause/Resume Organization Channel")
public class PauseResumeOrgChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should pause channel when user is owner")
    public void pauseOrgChannelSuccessWhenOwner() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has an active channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Active Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Pausing the channel
        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNEL_PAUSE_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be PAUSED
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse pausedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(pausedChannel.getStatus(), is("PAUSED"));
    }

    @Test
    @DisplayName("Should pause channel when user is admin")
    public void pauseOrgChannelSuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        // And: Organization has an active channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Active Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Pausing the channel as admin
        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNEL_PAUSE_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be PAUSED
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse pausedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(pausedChannel.getStatus(), is("PAUSED"));
    }

    @Test
    @DisplayName("Should fail when member tries to pause organization channel")
    public void pauseOrgChannelFailsWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Protected Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Pausing the channel as member
        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNEL_PAUSE_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when non-member tries to pause organization channel")
    public void pauseOrgChannelFailsWhenNonMember() throws Exception {
        // Given: An organization and a non-member user
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User nonMember = aValidatedUser();

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Protected Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Pausing the channel as non-member
        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNEL_PAUSE_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to pause organization channel")
    public void pauseOrgChannelSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Active Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Pausing the channel as global admin
        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNEL_PAUSE_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be PAUSED
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse pausedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(pausedChannel.getStatus(), is("PAUSED"));
    }

    @Test
    @DisplayName("Should be idempotent when pausing already paused channel")
    public void pauseOrgChannelIdempotentWhenAlreadyPaused() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has a paused channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Paused Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Channel is already paused
        mockMvc.perform(
            post(
                ORGANIZATION_CHANNEL_PAUSE_PATH
                    .replace("{orgId}", org.getId().toString())
                    .replace("{id}", createdChannel.getId().toString())
            )
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
        );

        // When: Pausing again
        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNEL_PAUSE_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel should still be PAUSED
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channel.getStatus(), is("PAUSED"));
    }

    @Test
    @DisplayName("Should resume channel when user is owner")
    public void resumeOrgChannelSuccessWhenOwner() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has a paused channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Paused Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Channel is paused
        mockMvc.perform(
            post(
                ORGANIZATION_CHANNEL_PAUSE_PATH
                    .replace("{orgId}", org.getId().toString())
                    .replace("{id}", createdChannel.getId().toString())
            )
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
        );

        // When: Resuming the channel
        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNEL_RESUME_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be ACTIVE
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse resumedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(resumedChannel.getStatus(), is("ACTIVE"));
    }

    @Test
    @DisplayName("Should resume channel when user is admin")
    public void resumeOrgChannelSuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        // And: Organization has a paused channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Paused Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Channel is paused
        mockMvc.perform(
            post(
                ORGANIZATION_CHANNEL_PAUSE_PATH
                    .replace("{orgId}", org.getId().toString())
                    .replace("{id}", createdChannel.getId().toString())
            )
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
        );

        // When: Resuming the channel as admin
        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNEL_RESUME_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be ACTIVE
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse resumedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(resumedChannel.getStatus(), is("ACTIVE"));
    }

    @Test
    @DisplayName("Should fail when member tries to resume organization channel")
    public void resumeOrgChannelFailsWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Organization has a paused channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Paused Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Resuming the channel as member
        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNEL_RESUME_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to resume organization channel")
    public void resumeOrgChannelSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: Organization has a paused channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Paused Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Channel is paused
        mockMvc.perform(
            post(
                ORGANIZATION_CHANNEL_PAUSE_PATH
                    .replace("{orgId}", org.getId().toString())
                    .replace("{id}", createdChannel.getId().toString())
            )
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
        );

        // When: Resuming the channel as global admin
        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNEL_RESUME_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be ACTIVE
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse resumedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(resumedChannel.getStatus(), is("ACTIVE"));
    }

    @Test
    @DisplayName("Should be idempotent when resuming already active channel")
    public void resumeOrgChannelIdempotentWhenAlreadyActive() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has an active channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Active Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Resuming active channel
        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNEL_RESUME_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel should still be ACTIVE
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channel.getStatus(), is("ACTIVE"));
    }

    @Test
    @DisplayName("Should fail when pausing non-existent channel")
    public void pauseOrgChannelFailsWhenNotFound() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Pausing non-existent channel
        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNEL_PAUSE_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", "99999")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when resuming non-existent channel")
    public void resumeOrgChannelFailsWhenNotFound() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Resuming non-existent channel
        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNEL_RESUME_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", "99999")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user pauses channel")
    public void pauseOrgChannelFailsWhenUnauthenticated() throws Exception {
        // Given: An organization with a channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Test Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Pausing channel without authentication
        final MockHttpServletRequestBuilder pauseRequest = post(
            ORGANIZATION_CHANNEL_PAUSE_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user resumes channel")
    public void resumeOrgChannelFailsWhenUnauthenticated() throws Exception {
        // Given: An organization with a channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Test Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Resuming channel without authentication
        final MockHttpServletRequestBuilder resumeRequest = post(
            ORGANIZATION_CHANNEL_RESUME_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }
}
