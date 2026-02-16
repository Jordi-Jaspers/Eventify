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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Integration Test - Delete Organization Channel")
public class DeleteOrgChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should delete channel when user is owner")
    public void deleteOrgChannelSuccessWhenOwner() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Channel to Delete")
            .setSlug("test.channel.1");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Deleting the channel
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be PENDING_DELETION
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse deletedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(deletedChannel.getStatus(), is("PENDING_DELETION"));
    }

    @Test
    @DisplayName("Should delete channel when user is admin")
    public void deleteOrgChannelSuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Channel to Delete")
            .setSlug("test.channel.2");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Deleting the channel as admin
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be PENDING_DELETION
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse deletedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(deletedChannel.getStatus(), is("PENDING_DELETION"));
    }

    @Test
    @DisplayName("Should fail when member tries to delete organization channel")
    public void deleteOrgChannelFailsWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Protected Channel")
            .setSlug("test.channel.3");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Deleting the channel as member
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when non-member tries to delete organization channel")
    public void deleteOrgChannelFailsWhenNonMember() throws Exception {
        // Given: An organization and a non-member user
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User nonMember = aValidatedUser();

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Protected Channel")
            .setSlug("test.channel.4");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Deleting the channel as non-member
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to delete organization channel")
    public void deleteOrgChannelSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Channel to Delete")
            .setSlug("test.channel.5");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Deleting the channel as global admin
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be PENDING_DELETION
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse deletedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(deletedChannel.getStatus(), is("PENDING_DELETION"));
    }

    @Test
    @DisplayName("Should fail when deleting non-existent channel")
    public void deleteOrgChannelFailsWhenNotFound() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Deleting non-existent channel
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", "99999")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when deleting already deleted channel")
    public void deleteOrgChannelFailsWhenAlreadyDeleted() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has a deleted channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Deleted Channel")
            .setSlug("test.channel.6");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Channel is already deleted
        mockMvc.perform(
            delete(
                ORGANIZATION_CHANNEL_PATH
                    .replace("{orgId}", org.getId().toString())
                    .replace("{id}", createdChannel.getId().toString())
            )
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
        );

        // When: Attempting to delete again
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when deleting channel from different organization")
    public void deleteOrgChannelFailsWhenDifferentOrg() throws Exception {
        // Given: Two organizations with different owners
        final User owner1 = aValidatedUser();
        final Organization org1 = anOrganisationWithOwner(owner1);
        final User owner2 = aValidatedUser();
        final Organization org2 = anOrganisationWithOwner(owner2);

        // And: Org1 has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Org1 Channel")
            .setSlug("test.channel.7");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org1.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner1.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Owner2 tries to delete Org1's channel using Org2 path
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org2.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner2.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NOT_FOUND (don't expose existence)
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user deletes organization channel")
    public void deleteOrgChannelFailsWhenUnauthenticated() throws Exception {
        // Given: An organization with a channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Test Channel")
            .setSlug("test.channel.8");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Deleting channel without authentication
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when organization not found")
    public void deleteOrgChannelFailsWhenOrgNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Deleting channel for non-existent organization
        final MockHttpServletRequestBuilder deleteRequest = delete(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", "99999")
                .replace("{id}", "1")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN or NOT_FOUND
        assertThat(
            response.andReturn().getResponse().getStatus(),
            anyOf(is(SC_FORBIDDEN), is(SC_NOT_FOUND))
        );
    }
}
