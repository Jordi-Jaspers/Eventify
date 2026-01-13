package io.github.eventify.api.channel.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.resource.ApiErrorResponseResource;
import io.github.jframe.exception.resource.ValidationErrorResponseResource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ORGANIZATION_CHANNELS_PATH;
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
@DisplayName("Integration Test - Create Organization Channel")
public class CreateOrgChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should create organization channel when user is owner")
    public void createOrgChannelSuccessWhenOwner() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // And: A valid create channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Production Errors")
            .setDescription("Critical error logs");

        // When: Creating organization channel
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getId(), is(notNullValue()));
        assertThat(channelResponse.getName(), is("Production Errors"));
        assertThat(channelResponse.getDescription(), is("Critical error logs"));
        assertThat(channelResponse.getStatus(), is("ACTIVE"));
        assertThat(channelResponse.getCreatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should create organization channel when user is admin")
    public void createOrgChannelSuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        // And: A valid create channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Admin Created Channel");

        // When: Creating organization channel as admin
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getName(), is("Admin Created Channel"));
        assertThat(channelResponse.getStatus(), is("ACTIVE"));
    }

    @Test
    @DisplayName("Should fail when member tries to create organization channel")
    public void createOrgChannelFailsWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: A valid create channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Member Channel");

        // When: Creating organization channel as member
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when non-member tries to create organization channel")
    public void createOrgChannelFailsWhenNonMember() throws Exception {
        // Given: An organization and a non-member user
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User nonMember = aValidatedUser();

        // And: A valid create channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Non-member Channel");

        // When: Creating organization channel as non-member
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to create organization channel")
    public void createOrgChannelSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid create channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Global Admin Channel");

        // When: Creating organization channel as global admin
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getName(), is("Global Admin Channel"));
    }

    @Test
    @DisplayName("Should create channel without description")
    public void createOrgChannelWithoutDescriptionSuccess() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // And: Request without description
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Simple Channel");

        // When: Creating organization channel
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Description should be null
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getDescription(), is(nullValue()));
    }

    @Test
    @DisplayName("Should fail when duplicate channel name in same organization")
    public void createOrgChannelFailsWhenDuplicateName() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // And: Organization already has channel named "Errors"
        final CreateChannelRequest firstRequest = new CreateChannelRequest()
            .setName("Errors");

        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(firstRequest))
        );

        // When: Creating another channel with same name
        final CreateChannelRequest duplicateRequest = new CreateChannelRequest()
            .setName("Errors");

        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
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
    @DisplayName("Should allow same channel name in different organizations")
    public void createOrgChannelSuccessWhenSameNameDifferentOrg() throws Exception {
        // Given: Two organizations with different owners
        final User owner1 = aValidatedUser();
        final Organization org1 = createOrganization(owner1);
        final User owner2 = aValidatedUser();
        final Organization org2 = createOrganization(owner2);

        // And: Org1 has channel named "Errors"
        final CreateChannelRequest request1 = new CreateChannelRequest()
            .setName("Errors");

        mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org1.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner1.getAccessToken().getValue())
                .content(toJson(request1))
        );

        // When: Org2 creates channel with same name
        final CreateChannelRequest request2 = new CreateChannelRequest()
            .setName("Errors");

        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org2.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner2.getAccessToken().getValue())
            .content(toJson(request2));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));
    }

    @Test
    @DisplayName("Should fail when channel name is empty")
    public void createOrgChannelFailsWhenNameIsEmpty() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // And: Request with empty name
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("");

        // When: Creating organization channel
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Validation error should contain name field
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);
        assertThat(error.getErrors().getFirst().getField(), is("name"));
    }

    @Test
    @DisplayName("Should fail when channel name exceeds 100 characters")
    public void createOrgChannelFailsWhenNameTooLong() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // And: Request with name exceeding 100 characters
        final String longName = "a".repeat(101);
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName(longName);

        // When: Creating organization channel
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when description exceeds 500 characters")
    public void createOrgChannelFailsWhenDescriptionTooLong() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // And: Request with description exceeding 500 characters
        final String longDescription = "a".repeat(501);
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Valid Name")
            .setDescription(longDescription);

        // When: Creating organization channel
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user creates organization channel")
    public void createOrgChannelFailsWhenUnauthenticated() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // And: A valid request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Unauthorized Channel");

        // When: Creating channel without authentication
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when organization not found")
    public void createOrgChannelFailsWhenOrgNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: A valid create channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Invalid Org Channel");

        // When: Creating channel for non-existent organization
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", "99999"))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be FORBIDDEN or NOT_FOUND
        assertThat(
            response.andReturn().getResponse().getStatus(),
            anyOf(is(SC_FORBIDDEN), is(SC_NOT_FOUND))
        );
    }
}
