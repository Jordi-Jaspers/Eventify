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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Integration Test - Get Organization Channel")
public class GetOrgChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should get channel by ID when user is owner")
    public void getOrgChannelByIdSuccessWhenOwner() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Test Channel")
            .setDescription("Test Description");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Getting channel by ID
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getId(), is(createdChannel.getId()));
        assertThat(channelResponse.getName(), is("Test Channel"));
        assertThat(channelResponse.getDescription(), is("Test Description"));
    }

    @Test
    @DisplayName("Should get channel by ID when user is admin")
    public void getOrgChannelByIdSuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Admin Viewable Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Getting channel by ID as admin
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getName(), is("Admin Viewable Channel"));
    }

    @Test
    @DisplayName("Should get channel by ID when user is member")
    public void getOrgChannelByIdSuccessWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Member Viewable Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Getting channel by ID as member
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getName(), is("Member Viewable Channel"));
    }

    @Test
    @DisplayName("Should fail when non-member tries to get organization channel")
    public void getOrgChannelByIdFailsWhenNonMember() throws Exception {
        // Given: An organization and a non-member user
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
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

        // When: Getting channel by ID as non-member
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to get organization channel")
    public void getOrgChannelByIdSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Global Admin Viewable Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Getting channel by ID as global admin
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getName(), is("Global Admin Viewable Channel"));
    }

    @Test
    @DisplayName("Should fail when getting non-existent channel")
    public void getOrgChannelByIdFailsWhenNotFound() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // When: Getting non-existent channel
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", "99999")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when getting channel from different organization")
    public void getOrgChannelByIdFailsWhenDifferentOrg() throws Exception {
        // Given: Two organizations with different owners
        final User owner1 = aValidatedUser();
        final Organization org1 = createOrganization(owner1);
        final User owner2 = aValidatedUser();
        final Organization org2 = createOrganization(owner2);

        // And: Org1 has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Org1 Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org1.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner1.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Owner2 tries to get Org1's channel using Org2 path
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org2.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner2.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be NOT_FOUND (don't expose existence)
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when getting deleted channel")
    public void getOrgChannelByIdFailsWhenDeleted() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // And: Organization has a deleted channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Deleted Channel");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Channel is deleted
        mockMvc.perform(
            delete(
                ORGANIZATION_CHANNEL_PATH
                    .replace("{orgId}", org.getId().toString())
                    .replace("{id}", createdChannel.getId().toString())
            )
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
        );

        // When: Getting deleted channel
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user gets organization channel")
    public void getOrgChannelByIdFailsWhenUnauthenticated() throws Exception {
        // Given: An organization with a channel
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

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

        // When: Getting channel without authentication
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when organization not found")
    public void getOrgChannelByIdFailsWhenOrgNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Getting channel for non-existent organization
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", "99999")
                .replace("{id}", "1")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be FORBIDDEN or NOT_FOUND
        assertThat(
            response.andReturn().getResponse().getStatus(),
            anyOf(is(SC_FORBIDDEN), is(SC_NOT_FOUND))
        );
    }
}
