package io.github.eventify.api.channel.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.resource.ApiErrorResponseResource;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Integration Test - Update Organization Channel")
public class UpdateOrgChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should update channel details when user is owner")
    public void updateOrgChannelSuccessWhenOwner() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Old Name");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Updating channel details
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("New Name")
            .setDescription("New Description");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse updatedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(updatedChannel.getName(), is("New Name"));
        assertThat(updatedChannel.getDescription(), is("New Description"));
        assertThat(updatedChannel.getUpdatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should update channel details when user is admin")
    public void updateOrgChannelSuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Old Name");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Updating channel details as admin
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("Admin Updated");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse updatedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(updatedChannel.getName(), is("Admin Updated"));
    }

    @Test
    @DisplayName("Should fail when member tries to update organization channel")
    public void updateOrgChannelFailsWhenMember() throws Exception {
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

        // When: Updating channel as member
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("Member Update");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when non-member tries to update organization channel")
    public void updateOrgChannelFailsWhenNonMember() throws Exception {
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

        // When: Updating channel as non-member
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("Non-member Update");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to update organization channel")
    public void updateOrgChannelSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: Organization has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Old Name");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Updating channel as global admin
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("Global Admin Updated");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse updatedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(updatedChannel.getName(), is("Global Admin Updated"));
    }

    @Test
    @DisplayName("Should fail when updating to duplicate channel name")
    public void updateOrgChannelFailsWhenDuplicateName() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has two channels
        final CreateChannelRequest createRequest1 = new CreateChannelRequest()
            .setName("Channel 1");

        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest1))
        );

        final CreateChannelRequest createRequest2 = new CreateChannelRequest()
            .setName("Channel 2");

        final ResultActions createResponse = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(createRequest2))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Updating Channel 2 to duplicate name "Channel 1"
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("Channel 1");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention duplicate name
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), containsStringIgnoringCase("duplicate"));
    }

    @Test
    @DisplayName("Should fail when updating non-existent channel")
    public void updateOrgChannelFailsWhenNotFound() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Update request
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("New Name");

        // When: Updating non-existent channel
        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", "99999")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when updating channel from different organization")
    public void updateOrgChannelFailsWhenDifferentOrg() throws Exception {
        // Given: Two organizations with different owners
        final User owner1 = aValidatedUser();
        final Organization org1 = anOrganisationWithOwner(owner1);
        final User owner2 = aValidatedUser();
        final Organization org2 = anOrganisationWithOwner(owner2);

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

        // When: Owner2 tries to update Org1's channel using Org2 path
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("Updated Name");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org2.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner2.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user updates organization channel")
    public void updateOrgChannelFailsWhenUnauthenticated() throws Exception {
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

        // When: Updating channel without authentication
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("Updated Name");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when organization not found")
    public void updateOrgChannelFailsWhenOrgNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Update request
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("Updated Name");

        // When: Updating channel for non-existent organization
        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            ORGANIZATION_CHANNEL_PATH
                .replace("{orgId}", "99999")
                .replace("{id}", "1")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be FORBIDDEN or NOT_FOUND
        assertThat(
            response.andReturn().getResponse().getStatus(),
            anyOf(is(SC_FORBIDDEN), is(SC_NOT_FOUND))
        );
    }
}
