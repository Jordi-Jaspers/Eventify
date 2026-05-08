package io.github.eventify.api.apikey.controller;

import io.github.eventify.api.apikey.model.request.CreateApiKeyRequest;
import io.github.eventify.api.apikey.model.response.ApiKeyCreationResponse;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.resource.ApiErrorResponseResource;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ORGANIZATION_API_KEYS_PATH;
import static io.github.eventify.api.Paths.ORGANIZATION_API_KEYS_SEARCH_PATH;
import static io.github.eventify.api.Paths.ORGANIZATION_API_KEY_PATH;
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
@DisplayName("Integration Test - Organization API Key Controller")
public class OrganizationApiKeyControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should create organization API key when user is owner")
    public void createOrgApiKeySuccessWhenOwner() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: A valid create API key request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Production Server Key");

        // When: Creating organization API key
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain org_ prefix key
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createResponse = fromJson(content, ApiKeyCreationResponse.class);

        assertThat(createResponse.getId(), is(notNullValue()));
        assertThat(createResponse.getName(), is("Production Server Key"));
        assertThat(createResponse.getKey(), startsWith("org_"));
        assertThat(createResponse.getKey().length(), is(36));
        assertThat(createResponse.getSuffix(), is(notNullValue()));
        assertThat(createResponse.getSuffix().length(), is(4));
        assertThat(createResponse.getCreatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should create organization API key when user is admin")
    public void createOrgApiKeySuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        // And: A valid create API key request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Admin Created Key");

        // When: Creating organization API key as admin
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain org_ prefix key
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createResponse = fromJson(content, ApiKeyCreationResponse.class);

        assertThat(createResponse.getKey(), startsWith("org_"));
    }

    @Test
    @DisplayName("Should fail when member tries to create organization API key")
    public void createOrgApiKeyFailsWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: A valid create API key request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Member Key");

        // When: Creating organization API key as member
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when non-member tries to create organization API key")
    public void createOrgApiKeyFailsWhenNonMember() throws Exception {
        // Given: An organization and a non-member user
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User nonMember = aValidatedUser();

        // And: A valid create API key request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Non-member Key");

        // When: Creating organization API key as non-member
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to create organization API key")
    public void createOrgApiKeySuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: A valid create API key request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Global Admin Key");

        // When: Creating organization API key as global admin
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain org_ prefix key
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createResponse = fromJson(content, ApiKeyCreationResponse.class);

        assertThat(createResponse.getKey(), startsWith("org_"));
    }

    @Test
    @DisplayName("Should create organization API key with expiration date")
    public void createOrgApiKeyWithExpirationSuccess() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: A request with future expiration
        final OffsetDateTime futureExpiration = OffsetDateTime.now().plusDays(30);
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Temporary Org Key")
            .setExpiresAt(futureExpiration);

        // When: Creating organization API key
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should include expiration date
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createResponse = fromJson(content, ApiKeyCreationResponse.class);

        assertThat(createResponse.getExpiresAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should create unlimited organization API keys")
    public void createOrgApiKeyUnlimitedSuccess() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Creating 10 organization API keys (more than user limit)
        for (int i = 0; i < 10; i++) {
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName("Org Key " + (i + 1));

            final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(request));

            final ResultActions response = mockMvc.perform(createRequest);

            // Then: All should be CREATED
            response.andExpect(status().is(SC_CREATED));
        }
    }

    @Test
    @DisplayName("Should search organization API keys when user is owner")
    public void searchOrgApiKeysSuccessWhenOwner() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has API keys
        final CreateApiKeyRequest request1 = new CreateApiKeyRequest()
            .setName("Production Key");
        final CreateApiKeyRequest request2 = new CreateApiKeyRequest()
            .setName("Development Key");

        mockMvc.perform(
            post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(request1))
        );

        mockMvc.perform(
            post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(request2))
        );

        // When: Searching organization API keys
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_API_KEYS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain paged results with masked keys
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("org_******"));
    }

    @Test
    @DisplayName("Should search organization API keys when user is member")
    public void searchOrgApiKeysSuccessWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Organization has API keys
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Shared Key");

        mockMvc.perform(
            post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(request))
        );

        // When: Searching organization API keys as member
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_API_KEYS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain paged results
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("\"content\":"));
    }

    @Test
    @DisplayName("Should fail when non-member tries to search organization API keys")
    public void searchOrgApiKeysFailsWhenNonMember() throws Exception {
        // Given: An organization and a non-member user
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User nonMember = aValidatedUser();

        // When: Searching organization API keys as non-member
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_API_KEYS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to search organization API keys")
    public void searchOrgApiKeysSuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: Organization has API keys
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Admin Viewable Key");

        mockMvc.perform(
            post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(request))
        );

        // When: Searching organization API keys as global admin
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_API_KEYS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain paged results
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("\"content\":"));
    }

    @Test
    @DisplayName("Should return empty page when organization has no API keys")
    public void searchOrgApiKeysReturnsEmptyWhenNoKeys() throws Exception {
        // Given: An organization with no API keys
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Searching organization API keys
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_API_KEYS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should be valid JSON (empty result set is fine)
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content.length(), greaterThan(0));
    }

    @Test
    @DisplayName("Should verify full key not included in search response")
    public void searchOrgApiKeysDoesNotIncludeFullKey() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has created an API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Test Key");

        mockMvc.perform(
            post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(request))
        );

        // When: Searching organization API keys
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_API_KEYS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should not contain full key
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, not(containsString("\"key\":")));
        assertThat(content, containsString("\"maskedKey\":"));
    }

    @Test
    @DisplayName("Should revoke organization API key when user is owner")
    public void revokeOrgApiKeySuccessWhenOwner() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Organization has created an API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Key to Revoke");

        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions createResponse = mockMvc.perform(createRequest);
        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createdKey = fromJson(createContent, ApiKeyCreationResponse.class);

        // When: Revoking the organization API key
        final MockHttpServletRequestBuilder revokeRequest = delete(
            ORGANIZATION_API_KEY_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{keyId}", createdKey.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should revoke organization API key when user is admin")
    public void revokeOrgApiKeySuccessWhenAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminUser = aValidatedUser();
        addMemberToOrganization(org, adminUser, OrganizationalRole.ADMIN);

        // And: Organization has created an API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Key to Revoke");

        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions createResponse = mockMvc.perform(createRequest);
        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createdKey = fromJson(createContent, ApiKeyCreationResponse.class);

        // When: Revoking the organization API key as admin
        final MockHttpServletRequestBuilder revokeRequest = delete(
            ORGANIZATION_API_KEY_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{keyId}", createdKey.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail when member tries to revoke organization API key")
    public void revokeOrgApiKeyFailsWhenMember() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Organization has created an API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Protected Key");

        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions createResponse = mockMvc.perform(createRequest);
        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createdKey = fromJson(createContent, ApiKeyCreationResponse.class);

        // When: Revoking the organization API key as member
        final MockHttpServletRequestBuilder revokeRequest = delete(
            ORGANIZATION_API_KEY_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{keyId}", createdKey.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow global admin to revoke organization API key")
    public void revokeOrgApiKeySuccessWhenGlobalAdmin() throws Exception {
        // Given: An organization and a global admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: Organization has created an API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Admin Revocable Key");

        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions createResponse = mockMvc.perform(createRequest);
        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createdKey = fromJson(createContent, ApiKeyCreationResponse.class);

        // When: Revoking the organization API key as global admin
        final MockHttpServletRequestBuilder revokeRequest = delete(
            ORGANIZATION_API_KEY_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{keyId}", createdKey.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail when API key not found")
    public void revokeOrgApiKeyFailsWhenNotFound() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Revoking non-existent API key
        final MockHttpServletRequestBuilder revokeRequest = delete(
            ORGANIZATION_API_KEY_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{keyId}", "99999")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));

        // And: Error message should mention API key
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getErrorMessage(), containsString("API key"));
    }

    @Test
    @DisplayName("Should fail when revoking API key from different organization")
    public void revokeOrgApiKeyFailsWhenDifferentOrg() throws Exception {
        // Given: Two organizations with different owners
        final User owner1 = aValidatedUser();
        final Organization org1 = anOrganisationWithOwner(owner1);
        final User owner2 = aValidatedUser();
        final Organization org2 = anOrganisationWithOwner(owner2);

        // And: Org1 has created an API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Org1 Key");

        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org1.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner1.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions createResponse = mockMvc.perform(createRequest);
        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createdKey = fromJson(createContent, ApiKeyCreationResponse.class);

        // When: Owner2 tries to revoke Org1's key using Org2 path
        final MockHttpServletRequestBuilder revokeRequest = delete(
            ORGANIZATION_API_KEY_PATH
                .replace("{orgId}", org2.getId().toString())
                .replace("{keyId}", createdKey.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner2.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be FORBIDDEN or NOT_FOUND
        assertThat(
            response.andReturn().getResponse().getStatus(),
            anyOf(is(SC_FORBIDDEN), is(SC_NOT_FOUND))
        );
    }

    @Test
    @DisplayName("Should fail when organization not found")
    public void createOrgApiKeyFailsWhenOrgNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: A valid create API key request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Invalid Org Key");

        // When: Creating API key for non-existent organization
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", "99999"))
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

    @Test
    @DisplayName("Should fail when searching keys for non-existent organization")
    public void searchOrgApiKeysFailsWhenOrgNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Searching keys for non-existent organization
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_API_KEYS_SEARCH_PATH.replace("{orgId}", "99999")
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
    @DisplayName("Should fail when unauthenticated user creates organization API key")
    public void createOrgApiKeyFailsWhenUnauthenticated() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: A valid request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Unauthorized Key");

        // When: Creating API key without authentication
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user searches organization API keys")
    public void searchOrgApiKeysFailsWhenUnauthenticated() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Searching keys without authentication
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder searchRequest = post(
            ORGANIZATION_API_KEYS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user revokes organization API key")
    public void revokeOrgApiKeyFailsWhenUnauthenticated() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Revoking key without authentication
        final MockHttpServletRequestBuilder revokeRequest = delete(
            ORGANIZATION_API_KEY_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{keyId}", "1")
        )
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail when API key name is empty")
    public void createOrgApiKeyFailsWhenNameIsEmpty() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Request with empty name
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("");

        // When: Creating organization API key
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when API key name exceeds 100 characters")
    public void createOrgApiKeyFailsWhenNameTooLong() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Request with name exceeding 100 characters
        final String longName = "a".repeat(101);
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName(longName);

        // When: Creating organization API key
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when expiration date is in the past")
    public void createOrgApiKeyFailsWhenExpirationInPast() throws Exception {
        // Given: An organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Request with past expiration
        final OffsetDateTime pastExpiration = OffsetDateTime.now().minusDays(1);
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Invalid Key")
            .setExpiresAt(pastExpiration);

        // When: Creating organization API key
        final MockHttpServletRequestBuilder createRequest = post(ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }
}
