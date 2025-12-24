package io.github.eventify.api.organization.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.api.organization.model.response.OrganizationResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ADMIN_ORGANIZATIONS_PATH;
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

@DisplayName("Integration Test - Admin Organization Controller")
public class AdminOrganizationControllerTest extends IntegrationTest {

    private static final String VALID_ORG_NAME_WITH_SPACES = "Test  Multiple   Spaces";
    private static final String BLANK_NAME = "   ";
    private static final String SHORT_NAME = "AB";
    private static final String LONG_NAME = "A".repeat(101);

    @Test
    @DisplayName("Should create organization with valid data")
    public void createOrganizationSuccess() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid organization request
        final ProvisionOrganizationRequest request = aValidProvisionOrganizationRequest();

        // When: Creating organization
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: The response should contain organization details
        final String content = response.andReturn().getResponse().getContentAsString();
        final OrganizationResponse organizationResponse = fromJson(content, OrganizationResponse.class);

        assertThat(organizationResponse.getId(), notNullValue());
        assertThat(organizationResponse.getName(), is(request.getName()));
        assertThat(organizationResponse.getSlug(), is(notNullValue()));
        assertThat(organizationResponse.getStatus(), is(OrganizationStatus.TRIAL));
        assertThat(organizationResponse.getCreatedBy(), notNullValue());
        assertThat(organizationResponse.getCreatedAt(), notNullValue());
    }

    @Test
    @DisplayName("Should handle slug collision with auto-increment suffix")
    public void createOrganizationWithSlugCollision() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A common organization name to cause slug collision
        final String commonOrgName = "Capsule Corp";

        // And: A first organization is created
        final ProvisionOrganizationRequest organizationRequest = aValidProvisionOrganizationRequest()
            .setName(commonOrgName);

        final MockHttpServletRequestBuilder firstCreateRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(organizationRequest))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        mockMvc.perform(firstCreateRequest).andExpect(status().is(SC_CREATED));

        // When: Creating second organization with same name
        final MockHttpServletRequestBuilder secondCreateRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(organizationRequest))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(secondCreateRequest);

        // Then: The response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: The second organization should have slug with suffix
        final String content = response.andReturn().getResponse().getContentAsString();
        final OrganizationResponse organizationResponse = fromJson(content, OrganizationResponse.class);

        assertThat(organizationResponse.getSlug(), is(commonOrgName.toLowerCase().replace(" ", "-") + "-1"));
    }

    @Test
    @DisplayName("Should not create organization without authentication")
    public void createOrganizationWithoutAuthenticationFails() throws Exception {
        // Given: A valid organization request with no authentication
        final ProvisionOrganizationRequest request = aValidProvisionOrganizationRequest();

        // When: Creating organization without authorization header
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should not create organization without required permission")
    public void createOrganizationWithoutPermissionFails() throws Exception {
        // Given: A regular user without PROVISION_ORGANIZATIONS permission
        final User regularUser = aValidatedUser();

        // And: A valid organization request
        final ProvisionOrganizationRequest request = aValidProvisionOrganizationRequest();

        // When: Regular user attempts to create organization
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should not create organization with blank name")
    public void createOrganizationWithBlankNameFails() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid owner
        final User owner = aValidatedUser();

        // And: A request with blank name
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName(BLANK_NAME);
        request.setOwner(owner.getEmail());

        // When: Creating organization with blank name
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should not create organization with null name")
    public void createOrganizationWithNullNameFails() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid owner
        final User owner = aValidatedUser();

        // And: A request with null name
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName(null);
        request.setOwner(owner.getEmail());

        // When: Creating organization with null name
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should not create organization with short name")
    public void createOrganizationWithShortNameFails() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid owner
        final User owner = aValidatedUser();

        // And: A request with short name (2 characters)
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName(SHORT_NAME);
        request.setOwner(owner.getEmail());

        // When: Creating organization with short name
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should not create organization with name exceeding max length")
    public void createOrganizationWithLongNameFails() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid owner
        final User owner = aValidatedUser();

        // And: A request with name exceeding 100 characters
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName(LONG_NAME);
        request.setOwner(owner.getEmail());

        // When: Creating organization with long name
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should accept organization name with exactly 3 characters")
    public void createOrganizationWithMinimumNameLength() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid owner
        final User owner = aValidatedUser();

        // And: A request with 3-character name
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName("ABC");
        request.setOwner(owner.getEmail());

        // When: Creating organization
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be CREATED
        response.andExpect(status().is(SC_CREATED));
    }

    @Test
    @DisplayName("Should accept organization name with exactly 100 characters")
    public void createOrganizationWithMaximumNameLength() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid owner
        final User owner = aValidatedUser();

        // And: A request with 100-character name
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest()
            .setName("A".repeat(100))
            .setOwner(owner.getEmail());

        // When: Creating organization
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be CREATED
        response.andExpect(status().is(SC_CREATED));
    }

    @Test
    @DisplayName("Should handle organization name with special characters")
    public void createOrganizationWithSpecialCharactersInName() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid owner
        final User owner = aValidatedUser();

        // And: A request with special characters in name
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest()
            .setName("Test & Co.")
            .setOwner(owner.getEmail());

        // When: Creating organization
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be CREATED
        response.andExpect(status().is(SC_CREATED));
    }

    @Test
    @DisplayName("Should handle organization name with multiple spaces")
    public void createOrganizationWithMultipleSpacesInName() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid owner
        final User owner = aValidatedUser();

        // And: A request with multiple spaces in name
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest()
            .setName(VALID_ORG_NAME_WITH_SPACES)
            .setOwner(owner.getEmail());

        // When: Creating organization
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: The slug should have normalized spaces
        final String content = response.andReturn().getResponse().getContentAsString();
        final OrganizationResponse organizationResponse = fromJson(content, OrganizationResponse.class);

        assertThat(organizationResponse.getSlug(), is("test-multiple-spaces"));
    }

    @Test
    @DisplayName("Should create organization with owner successfully")
    public void createOrganizationWithOwnerSuccess() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid owner user
        final User owner = aValidatedUser();

        // And: A valid organization request with owner
        final ProvisionOrganizationRequest request = aValidProvisionOrganizationRequestWithOwner(owner.getEmail());

        // When: Creating organization with owner
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: The response should contain organization and owner details
        final String content = response.andReturn().getResponse().getContentAsString();
        final OrganizationResponse organizationResponse = fromJson(content, OrganizationResponse.class);

        assertThat(organizationResponse.getId(), notNullValue());
        assertThat(organizationResponse.getName(), is(request.getName()));
        assertThat(organizationResponse.getOwner(), notNullValue());
        assertThat(organizationResponse.getOwner().getId(), is(owner.getId()));
        assertThat(organizationResponse.getOwner().getEmail(), is(owner.getEmail()));
        assertThat(organizationResponse.getOwner().getFirstName(), is(owner.getFirstName()));
        assertThat(organizationResponse.getOwner().getLastName(), is(owner.getLastName()));
    }

    @Test
    @DisplayName("Should not create organization without owner")
    public void createOrganizationWithoutOwnerFails() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A request without owner field
        final ProvisionOrganizationRequest request = aValidProvisionOrganizationRequestWithOwner(null);

        // When: Creating organization without owner
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should not create organization with non-existent owner")
    public void createOrganizationWithNonExistentOwnerFails() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A request with non-existent owner email
        final String nonExistentEmail = "nonexistent@example.com";
        final ProvisionOrganizationRequest request = aValidProvisionOrganizationRequestWithOwner(nonExistentEmail);

        // When: Creating organization with non-existent owner
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should not create organization with disabled owner")
    public void createOrganizationWithDisabledOwnerFails() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A disabled owner user
        final User disabledOwner = aLockedUser();

        // And: A request with disabled owner
        final ProvisionOrganizationRequest request = aValidProvisionOrganizationRequestWithOwner(disabledOwner.getEmail());

        // When: Creating organization with disabled owner
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should not create organization with invalid owner email format")
    public void createOrganizationWithInvalidOwnerEmailFails() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A request with invalid email format
        final String invalidEmail = "not-an-email";
        final ProvisionOrganizationRequest request = aValidProvisionOrganizationRequestWithOwner(invalidEmail);

        // When: Creating organization with invalid owner email
        final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }
}
