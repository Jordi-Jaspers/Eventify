package io.github.eventify.api.admin.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.api.organization.model.response.OrganizationResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ADMIN_ORGANIZATIONS_PATH;
import static io.github.eventify.api.Paths.ADMIN_ORGANIZATIONS_SEARCH_PATH;
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

    // ============================= LIST ORGANIZATIONS TESTS =============================

    @Test
    @DisplayName("Should list organizations with default pagination")
    public void listOrganizationsSuccess() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Multiple test organizations
        createOrganizationWithStatus("Alpha Corp", OrganizationStatus.TRIAL);
        createOrganizationWithStatus("Beta Inc", OrganizationStatus.ACTIVE);
        createOrganizationWithStatus("Gamma LLC", OrganizationStatus.SUSPENDED);

        // And: A search request with default pagination
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        // When: Listing organizations
        final MockHttpServletRequestBuilder request = post(ADMIN_ORGANIZATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The response should contain organizations
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationResponse> pageResponse = fromJson(
            content,
            new TypeReference<PageResource<OrganizationResponse>>() {}
        );

        assertThat(pageResponse.getContent(), hasSize(greaterThanOrEqualTo(3)));
        assertThat(pageResponse.getTotalElements(), greaterThanOrEqualTo(3L));
    }

    @Test
    @DisplayName("Should list organizations with custom pagination")
    public void listOrganizationsWithPagination() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Five test organizations
        for (int i = 0; i < 5; i++) {
            createOrganizationWithStatus("Org " + i, OrganizationStatus.TRIAL);
        }

        // And: A search request with custom pagination
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(1);
        searchInput.setPageSize(2);

        // When: Requesting page 1 with size 2
        final MockHttpServletRequestBuilder request = post(ADMIN_ORGANIZATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The response should contain exactly 2 organizations
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationResponse> pageResponse = fromJson(
            content,
            new TypeReference<PageResource<OrganizationResponse>>() {}
        );

        assertThat(pageResponse.getContent(), hasSize(2));
        assertThat(pageResponse.getPageNumber(), is(1));
        assertThat(pageResponse.getPageSize(), is(2));
        assertThat(pageResponse.getTotalElements(), greaterThanOrEqualTo(5L));
    }

    @Test
    @DisplayName("Should search organizations by name case-insensitively")
    public void searchOrganizationsByName() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Organizations with similar names
        createOrganizationWithStatus("TechCorp Solutions", OrganizationStatus.TRIAL);
        createOrganizationWithStatus("Medical Tech Inc", OrganizationStatus.ACTIVE);
        createOrganizationWithStatus("Financial Services", OrganizationStatus.TRIAL);

        // And: A search request for "tech"
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput nameFilter = new SearchInput();
        nameFilter.setFieldName("name");
        nameFilter.setTextValue("tech");
        searchInput.getSearchInputs().add(nameFilter);

        // When: Searching for "tech" (lowercase)
        final MockHttpServletRequestBuilder request = post(ADMIN_ORGANIZATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The response should only contain organizations with "tech" in name
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationResponse> pageResponse = fromJson(
            content,
            new TypeReference<PageResource<OrganizationResponse>>() {}
        );

        assertThat(pageResponse.getContent(), hasSize(greaterThanOrEqualTo(2)));
        pageResponse.getContent().forEach(
            org -> assertThat(org.getName().toLowerCase(), containsString("tech"))
        );
    }

    @Test
    @DisplayName("Should filter organizations by status")
    public void filterOrganizationsByStatus() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Organizations with different statuses
        createOrganizationWithStatus("Trial Org 1", OrganizationStatus.TRIAL);
        createOrganizationWithStatus("Trial Org 2", OrganizationStatus.TRIAL);
        createOrganizationWithStatus("Active Org", OrganizationStatus.ACTIVE);
        createOrganizationWithStatus("Suspended Org", OrganizationStatus.SUSPENDED);

        // And: A search request filtering by TRIAL status
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName("status");
        statusFilter.setTextValueList(List.of("TRIAL"));
        searchInput.getSearchInputs().add(statusFilter);

        // When: Filtering by TRIAL status
        final MockHttpServletRequestBuilder request = post(ADMIN_ORGANIZATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The response should only contain TRIAL organizations
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationResponse> pageResponse = fromJson(
            content,
            new TypeReference<PageResource<OrganizationResponse>>() {}
        );

        assertThat(pageResponse.getContent(), hasSize(greaterThanOrEqualTo(2)));
        pageResponse.getContent().forEach(
            org -> assertThat(org.getStatus(), is(OrganizationStatus.TRIAL))
        );
    }

    @Test
    @DisplayName("Should combine search and status filter")
    public void searchAndFilterCombined() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Various organizations
        createOrganizationWithStatus("Tech Alpha", OrganizationStatus.TRIAL);
        createOrganizationWithStatus("Tech Beta", OrganizationStatus.ACTIVE);
        createOrganizationWithStatus("Tech Gamma", OrganizationStatus.TRIAL);
        createOrganizationWithStatus("Medical Alpha", OrganizationStatus.TRIAL);

        // And: A search request with both name and status filters
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput nameFilter = new SearchInput();
        nameFilter.setFieldName("name");
        nameFilter.setTextValue("tech");
        searchInput.getSearchInputs().add(nameFilter);

        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName("status");
        statusFilter.setTextValueList(List.of("TRIAL"));
        searchInput.getSearchInputs().add(statusFilter);

        // When: Searching for "tech" AND filtering by TRIAL
        final MockHttpServletRequestBuilder request = post(ADMIN_ORGANIZATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The response should contain organizations matching both criteria
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationResponse> pageResponse = fromJson(
            content,
            new TypeReference<PageResource<OrganizationResponse>>() {}
        );

        assertThat(pageResponse.getContent(), hasSize(greaterThanOrEqualTo(2)));
        pageResponse.getContent().forEach(org -> {
            assertThat(org.getName().toLowerCase(), containsString("tech"));
            assertThat(org.getStatus(), is(OrganizationStatus.TRIAL));
        });
    }

    @Test
    @DisplayName("Should return organization with correct member count")
    public void listOrganizationsWithMemberCount() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: An organization with owner
        final User owner = aValidatedUser();
        final ProvisionOrganizationRequest provisionRequest = aValidProvisionOrganizationRequestWithOwner(owner.getEmail());
        createOrganization(provisionRequest);

        // And: A search request filtering by organization name
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput nameFilter = new SearchInput();
        nameFilter.setFieldName("name");
        nameFilter.setTextValue(provisionRequest.getName());
        searchInput.getSearchInputs().add(nameFilter);

        // When: Listing organizations
        final MockHttpServletRequestBuilder request = post(ADMIN_ORGANIZATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The organization should have memberCount of 1
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationResponse> pageResponse = fromJson(
            content,
            new TypeReference<PageResource<OrganizationResponse>>() {}
        );

        assertThat(pageResponse.getContent(), hasSize(1));
        final OrganizationResponse org = pageResponse.getContent().get(0);
        assertThat(org.getMemberCount(), is(1));
    }

    @Test
    @DisplayName("Should return empty result when no organizations match")
    public void listOrganizationsEmptyResult() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A search request for non-existent organization
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput nameFilter = new SearchInput();
        nameFilter.setFieldName("name");
        nameFilter.setTextValue("NonExistentOrganizationXYZ12345");
        searchInput.getSearchInputs().add(nameFilter);

        // When: Searching for non-existent organization
        final MockHttpServletRequestBuilder request = post(ADMIN_ORGANIZATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The response should be empty
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationResponse> pageResponse = fromJson(
            content,
            new TypeReference<PageResource<OrganizationResponse>>() {}
        );

        assertThat(pageResponse.getContent(), anyOf(nullValue(), hasSize(0)));
        assertThat(pageResponse.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should not list organizations without authentication")
    public void listOrganizationsWithoutAuthenticationFails() throws Exception {
        // Given: No authentication token
        // And: A search request with default pagination
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        // When: Listing organizations without authorization header
        final MockHttpServletRequestBuilder request = post(ADMIN_ORGANIZATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should not list organizations without permission")
    public void listOrganizationsWithoutPermissionFails() throws Exception {
        // Given: A regular user without MANAGE_ORGANIZATIONS permission
        final User regularUser = aValidatedUser();

        // And: A search request with default pagination
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        // When: Regular user attempts to list organizations
        final MockHttpServletRequestBuilder request = post(ADMIN_ORGANIZATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput))
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ============================= HELPER METHODS =============================

    /**
     * Helper method to create an organization with specific status.
     */
    private Organization createOrganizationWithStatus(final String name, final OrganizationStatus status) {
        final User owner = aValidatedUser();
        final String suffix = UUID.randomUUID().toString().substring(0, 5);
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest()
            .setName(INTEGRATION_PREFIX + name + "-" + suffix)
            .setOwner(owner.getEmail());

        final Organization org = createOrganization(request);
        org.setStatus(status);
        return organizationRepository.save(org);
    }

    /**
     * Helper method to create an organization from a request.
     */
    private Organization createOrganization(final ProvisionOrganizationRequest request) {
        try {
            final MockHttpServletRequestBuilder createRequest = post(ADMIN_ORGANIZATIONS_PATH)
                .contentType(APPLICATION_JSON)
                .content(toJson(request))
                .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

            final ResultActions response = mockMvc.perform(createRequest);
            final String content = response.andReturn().getResponse().getContentAsString();
            final OrganizationResponse organizationResponse = fromJson(content, OrganizationResponse.class);

            return organizationRepository.findById(organizationResponse.getId()).orElseThrow();
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create organization", e);
        }
    }
}
