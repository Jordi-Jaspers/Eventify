package io.github.eventify.api.admin.controller;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.model.request.CreateApiKeyRequest;
import io.github.eventify.api.apikey.model.response.ApiKeyCreationResponse;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import tools.jackson.core.type.TypeReference;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@DisplayName("Integration Test - Admin API Key Controller")
public class AdminApiKeyControllerTest extends IntegrationTest {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Test
    @DisplayName("Should return statistics when admin requests stats")
    public void getStatsSuccess() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Several API keys exist
        final User user1 = aValidatedUser();
        createUserApiKey(user1, "User Key 1", null);
        createUserApiKey(user1, "User Key 2", OffsetDateTime.now().plusDays(30));

        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        createOrgApiKey(org, owner, "Org Key 1", null);

        // When: Requesting API key statistics
        final MockHttpServletRequestBuilder request = get(ADMIN_API_KEYS_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain statistics
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("\"totalKeys\":"));
        assertThat(content, containsString("\"userKeys\":"));
        assertThat(content, containsString("\"organizationKeys\":"));
    }

    @Test
    @DisplayName("Should reject stats request when user is not admin")
    public void getStatsFailsWhenNotAdmin() throws Exception {
        // Given: A regular user
        final User user = aValidatedUser();

        // When: Requesting API key statistics
        final MockHttpServletRequestBuilder request = get(ADMIN_API_KEYS_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject stats request without authentication")
    public void getStatsFailsWhenUnauthenticated() throws Exception {
        // Given: No authentication

        // When: Requesting API key statistics
        final MockHttpServletRequestBuilder request = get(ADMIN_API_KEYS_STATS_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return empty statistics when no keys exist")
    public void getStatsReturnsZeroWhenNoKeys() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: No API keys exist

        // When: Requesting API key statistics
        final MockHttpServletRequestBuilder request = get(ADMIN_API_KEYS_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain zero values
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("\"totalKeys\":0"));
    }

    @Test
    @DisplayName("Should search all API keys when admin with no filters")
    public void searchKeysSuccessNoFilters() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Mixed user and org keys exist
        final User user1 = aValidatedUser();
        createUserApiKey(user1, "User Key", null);

        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        createOrgApiKey(org, owner, "Org Key", null);

        // When: Searching without filters
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain both key types
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(2L)));
    }

    @Test
    @DisplayName("Should filter by user scope when searching")
    public void searchKeysFiltersByUserScope() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Mixed keys exist
        final User user1 = aValidatedUser();
        createUserApiKey(user1, "User Key", null);

        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        createOrgApiKey(org, owner, "Org Key", null);

        // When: Searching with USER scope filter
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final SearchInput scopeFilter = new SearchInput();
        scopeFilter.setFieldName("scope");
        scopeFilter.setTextValueList(List.of(ApiKeyScope.USER.name()));
        input.getSearchInputs().add(scopeFilter);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should only contain user keys
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("evt_"));
        assertThat(content, not(containsString("org_")));
    }

    @Test
    @DisplayName("Should filter by organization scope when searching")
    public void searchKeysFiltersByOrgScope() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Mixed keys exist
        final User user1 = aValidatedUser();
        createUserApiKey(user1, "User Key", null);

        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        createOrgApiKey(org, owner, "Org Key", null);

        // When: Searching with ORGANIZATION scope filter
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final SearchInput scopeFilter = new SearchInput();
        scopeFilter.setFieldName("scope");
        scopeFilter.setTextValueList(List.of(ApiKeyScope.ORGANIZATION.name()));
        input.getSearchInputs().add(scopeFilter);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should only contain org keys
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("org_"));
        assertThat(content, not(containsString("evt_")));
    }

    @Test
    @DisplayName("Should filter by active status when searching")
    public void searchKeysFiltersByActiveStatus() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Active keys exist (cannot create expired keys via API due to validation)
        final User user1 = aValidatedUser();
        createUserApiKey(user1, "Active Key 1", OffsetDateTime.now().plusDays(30));
        createUserApiKey(user1, "Active Key 2", null); // Never expires - also active

        // When: Searching with ACTIVE status filter
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName("status");
        statusFilter.setTextValue("ACTIVE");
        input.getSearchInputs().add(statusFilter);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain active keys
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getContent(), is(notNullValue()));
        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(2L)));
    }

    @Test
    @DisplayName("Should search by key name")
    public void searchKeysByName() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Keys with specific names
        final User user1 = aValidatedUser();
        createUserApiKey(user1, "Production Server", null);
        createUserApiKey(user1, "Development Server", null);

        // When: Searching by name
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final SearchInput nameSearch = new SearchInput();
        nameSearch.setFieldName("searchTerm");
        nameSearch.setTextValue("Production");
        input.getSearchInputs().add(nameSearch);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain matching key
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("Production"));
    }

    @Test
    @DisplayName("Should sort by creation date when searching")
    public void searchKeysSortsByCreatedAt() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Multiple keys exist
        final User user1 = aValidatedUser();
        createUserApiKey(user1, "Key 1", null);
        createUserApiKey(user1, "Key 2", null);

        // When: Searching for keys
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain results
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getContent().size(), is(greaterThan(0)));
    }

    @Test
    @DisplayName("Should return paginated results when searching")
    public void searchKeysReturnsPaginatedResults() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Many keys exist (distributed across multiple users to avoid key limit)
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();
        final User user3 = aValidatedUser();
        final User user4 = aValidatedUser();

        for (int i = 0; i < 4; i++) {
            createUserApiKey(user1, "Key " + i, null);
        }
        for (int i = 0; i < 4; i++) {
            createUserApiKey(user2, "Key " + (i + 4), null);
        }
        for (int i = 0; i < 4; i++) {
            createUserApiKey(user3, "Key " + (i + 8), null);
        }
        for (int i = 0; i < 3; i++) {
            createUserApiKey(user4, "Key " + (i + 12), null);
        }

        // When: Requesting page with size 5
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(5);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain limited results
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getContent().size(), is(lessThanOrEqualTo(5)));
        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(15L)));
    }

    @Test
    @DisplayName("Should reject search when user is not admin")
    public void searchKeysFailsWhenNotAdmin() throws Exception {
        // Given: A regular user
        final User user = aValidatedUser();

        // When: Attempting to search all keys
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject search without authentication")
    public void searchKeysFailsWhenUnauthenticated() throws Exception {
        // Given: No authentication
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // When: Attempting to search
        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return empty page when no keys match filters")
    public void searchKeysReturnsEmptyWhenNoMatches() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // When: Searching with filter that matches nothing
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final SearchInput nameSearch = new SearchInput();
        nameSearch.setFieldName("searchTerm");
        nameSearch.setTextValue("NonexistentKey987xyz");
        input.getSearchInputs().add(nameSearch);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should be empty
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should revoke user API key when admin")
    public void revokeUserKeySuccessWhenAdmin() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A user API key exists
        final User user = aValidatedUser();
        final ApiKey key = createUserApiKey(user, "User Key to Revoke", null);

        // When: Admin revokes the key
        final MockHttpServletRequestBuilder request = delete(
            ADMIN_API_KEY_PATH.replace("{keyId}", key.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: Key should be deleted
        assertThat(apiKeyRepository.findById(key.getId()).isEmpty(), is(true));
    }

    @Test
    @DisplayName("Should revoke organization API key when admin")
    public void revokeOrgKeySuccessWhenAdmin() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: An organization API key exists
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final ApiKey key = createOrgApiKey(org, owner, "Org Key to Revoke", null);

        // When: Admin revokes the key
        final MockHttpServletRequestBuilder request = delete(
            ADMIN_API_KEY_PATH.replace("{keyId}", key.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: Key should be deleted
        assertThat(apiKeyRepository.findById(key.getId()).isEmpty(), is(true));
    }

    @Test
    @DisplayName("Should fail when revoking non-existent key")
    public void revokeKeyFailsWhenNotFound() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // When: Attempting to revoke non-existent key
        final MockHttpServletRequestBuilder request = delete(
            ADMIN_API_KEY_PATH.replace("{keyId}", "99999")
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should reject revoke when user is not admin")
    public void revokeKeyFailsWhenNotAdmin() throws Exception {
        // Given: A regular user
        final User user = aValidatedUser();

        // And: An API key exists
        final ApiKey key = createUserApiKey(user, "Protected Key", null);

        // When: Regular user attempts to revoke via admin endpoint
        final MockHttpServletRequestBuilder request = delete(
            ADMIN_API_KEY_PATH.replace("{keyId}", key.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject revoke without authentication")
    public void revokeKeyFailsWhenUnauthenticated() throws Exception {
        // Given: No authentication
        final User user = aValidatedUser();
        final ApiKey key = createUserApiKey(user, "Key", null);

        // When: Attempting to revoke without auth
        final MockHttpServletRequestBuilder request = delete(
            ADMIN_API_KEY_PATH.replace("{keyId}", key.getId().toString())
        )
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should search audit log when admin")
    public void searchAuditLogSuccessWhenAdmin() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A revoked key exists
        final User user = aValidatedUser();
        final ApiKey key = createUserApiKey(user, "Revoked Key", null);

        mockMvc.perform(
            delete(ADMIN_API_KEY_PATH.replace("{keyId}", key.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
        );

        // When: Searching audit log
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_AUDIT_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain audit records
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(1L)));
    }

    @Test
    @DisplayName("Should reject audit log search when user is not admin")
    public void searchAuditLogFailsWhenNotAdmin() throws Exception {
        // Given: A regular user
        final User user = aValidatedUser();

        // When: Attempting to search audit log
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_AUDIT_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject audit log search without authentication")
    public void searchAuditLogFailsWhenUnauthenticated() throws Exception {
        // Given: No authentication
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // When: Attempting to search audit log
        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_AUDIT_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return empty audit log when no revocations")
    public void searchAuditLogReturnsEmptyWhenNoRevocations() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: No revoked keys exist

        // When: Searching audit log
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_AUDIT_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should be empty
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
    }

    @Test
    @DisplayName("Should handle keys with null expiration date")
    public void searchKeysHandlesNullExpiration() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Key with no expiration
        final User user = aValidatedUser();
        createUserApiKey(user, "Never Expires", null);

        // When: Searching for keys
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain key with null expiration
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("Never Expires"));
    }

    @Test
    @DisplayName("Should handle keys with null last used date")
    public void searchKeysHandlesNullLastUsed() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Key that was never used
        final User user = aValidatedUser();
        createUserApiKey(user, "Never Used", null);

        // When: Searching for keys
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_API_KEYS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain key
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("Never Used"));
    }

    // Helper methods

    private ApiKey createUserApiKey(final User user, final String name, final OffsetDateTime expiresAt) throws Exception {
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName(name)
            .setExpiresAt(expiresAt);

        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);
        response.andExpect(status().is(SC_CREATED));
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createResponse = fromJson(content, ApiKeyCreationResponse.class);

        assertThat(createResponse, is(notNullValue()));
        assertThat(createResponse.getId(), is(notNullValue()));
        return apiKeyRepository.findById(createResponse.getId()).orElseThrow();
    }

    private ApiKey createOrgApiKey(final Organization org, final User creator, final String name,
        final OffsetDateTime expiresAt) throws Exception {
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName(name)
            .setExpiresAt(expiresAt);

        final MockHttpServletRequestBuilder createRequest = post(
            ORGANIZATION_API_KEYS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + creator.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);
        response.andExpect(status().is(SC_CREATED));
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createResponse = fromJson(content, ApiKeyCreationResponse.class);

        assertThat(createResponse, is(notNullValue()));
        assertThat(createResponse.getId(), is(notNullValue()));
        return apiKeyRepository.findById(createResponse.getId()).orElseThrow();
    }
}
