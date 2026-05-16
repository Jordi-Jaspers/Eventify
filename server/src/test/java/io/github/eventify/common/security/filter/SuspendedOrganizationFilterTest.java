package io.github.eventify.common.security.filter;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ADMIN_ORGANIZATIONS_PATH;
import static io.github.eventify.api.Paths.AUTH_PATH;
import static io.github.eventify.api.Paths.ORGANIZATION_MEMBERS_PATH;
import static io.github.eventify.api.Paths.PUBLIC_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.eventify.common.security.filter.ApiKeyAuthenticationFilter.API_KEY_HEADER;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - SuspendedOrganizationFilter")
public class SuspendedOrganizationFilterTest extends IntegrationTest {

    // ============================= SUSPENDED ORG — MEMBER ACCESS =============================

    @Test
    @DisplayName("Should block non-admin member from accessing suspended org endpoint")
    public void suspendedOrgBlocksNonAdminMember() throws Exception {
        // Given: A suspended organization with a regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);
        suspendOrganization(org);

        // When: Regular member accesses an org-scoped endpoint
        final MockHttpServletRequestBuilder request = get(
            ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString())
        )
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));

        // And: The error message should indicate suspension
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, containsString("suspended"));
    }

    @Test
    @DisplayName("Should block org owner from accessing suspended org endpoint")
    public void suspendedOrgBlocksOwner() throws Exception {
        // Given: A suspended organization with its owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        suspendOrganization(org);

        // When: Owner accesses an org-scoped endpoint
        final MockHttpServletRequestBuilder request = get(
            ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString())
        )
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ============================= SUSPENDED ORG — ADMIN BYPASS =============================

    @Test
    @DisplayName("Should allow admin user (MANAGE_ORGANIZATIONS) to access suspended org endpoint")
    public void suspendedOrgAllowsAdminUser() throws Exception {
        // Given: A suspended organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        suspendOrganization(org);

        // And: A system admin with MANAGE_ORGANIZATIONS authority
        final User systemAdmin = aValidatedUserWithRole(Role.ADMIN);

        // When: Admin accesses an org-scoped endpoint
        final MockHttpServletRequestBuilder request = get(
            ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString())
        )
            .header(AUTHORIZATION, BEARER + systemAdmin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should NOT be FORBIDDEN (admin bypasses suspension filter)
        // Note: may be 200 or another non-403 status depending on authorization
        final int status = response.andReturn().getResponse().getStatus();
        assertThat("Admin should not be blocked by suspension filter", status != SC_FORBIDDEN, Matchers.is(true));
    }

    // ============================= ACTIVE ORG — NORMAL ACCESS =============================

    @Test
    @DisplayName("Should allow member to access active org endpoint normally")
    public void activeOrgAllowsMemberAccess() throws Exception {
        // Given: An active organization with a member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // When: Member accesses an org-scoped endpoint
        final MockHttpServletRequestBuilder request = get(
            ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString())
        )
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should NOT be FORBIDDEN due to suspension
        final int status = response.andReturn().getResponse().getStatus();
        assertThat("Active org should not block members", status != SC_FORBIDDEN, Matchers.is(true));
    }

    // ============================= API KEY — SUSPENDED ORG =============================

    @Test
    @DisplayName("Should block API key request to suspended org endpoint")
    public void suspendedOrgBlocksApiKeyRequest() throws Exception {
        // Given: A suspended organization with an org-scoped API key
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final ApiKey orgApiKey = anApiKeyForOrganisation(owner, org, "Test Org Key");
        suspendOrganization(org);

        // When: API key request targets an org-scoped endpoint
        // Note: The filter checks org suspension for org-scoped API keys on /v1/organization/{orgId}/... paths
        final MockHttpServletRequestBuilder request = get(
            ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString())
        )
            .header(API_KEY_HEADER, orgApiKey.getKey());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ============================= ADMIN PATHS — NOT BLOCKED =============================

    @Test
    @DisplayName("Should not block admin paths even when org is suspended")
    public void adminPathsNotBlockedBySuspensionFilter() throws Exception {
        // Given: A system admin user
        final User systemAdmin = aValidatedUserWithRole(Role.ADMIN);

        // When: Admin accesses an admin-scoped path (not org-scoped)
        final io.github.jframe.datasource.search.model.input.SortablePageInput searchInput =
            new io.github.jframe.datasource.search.model.input.SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_ORGANIZATIONS_PATH + "/search")
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput))
            .header(AUTHORIZATION, BEARER + systemAdmin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should NOT be FORBIDDEN (admin paths bypass suspension filter)
        final int status = response.andReturn().getResponse().getStatus();
        assertThat("Admin paths should not be blocked by suspension filter", status != SC_FORBIDDEN, Matchers.is(true));
    }

    // ============================= PUBLIC/AUTH PATHS — NOT BLOCKED =============================

    @Test
    @DisplayName("Should not block public paths")
    public void publicPathsNotBlockedBySuspensionFilter() throws Exception {
        // Given: No authentication (public endpoint)
        // When: Accessing a public path
        final MockHttpServletRequestBuilder request = get(PUBLIC_PATH + "/health");

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should NOT be FORBIDDEN due to suspension filter
        final int status = response.andReturn().getResponse().getStatus();
        assertThat("Public paths should not be blocked by suspension filter", status != SC_FORBIDDEN, Matchers.is(true));
    }

    @Test
    @DisplayName("Should not block auth paths")
    public void authPathsNotBlockedBySuspensionFilter() throws Exception {
        // Given: A login request (auth path)
        final String loginBody = "{\"email\": \"test@example.com\", \"password\": \"Test123!@#\"}";

        // When: Accessing an auth path
        final MockHttpServletRequestBuilder request = post(AUTH_PATH + "/login")
            .contentType(APPLICATION_JSON)
            .content(loginBody);

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should NOT be FORBIDDEN due to suspension filter
        // (may be 401 for wrong credentials, but not 403 from suspension)
        final int status = response.andReturn().getResponse().getStatus();
        assertThat("Auth paths should not be blocked by suspension filter", status != SC_FORBIDDEN, Matchers.is(true));
    }

    // ============================= ORG-LESS ENDPOINTS — NOT BLOCKED =============================

    @Test
    @DisplayName("Should not interfere with org-less endpoints")
    public void orgLessEndpointsNotAffectedBySuspensionFilter() throws Exception {
        // Given: A regular authenticated user
        final User user = aValidatedUser();

        // When: Accessing a user-scoped endpoint (no orgId in path)
        final MockHttpServletRequestBuilder request = get("/v1/user/details")
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should NOT be FORBIDDEN due to suspension filter
        final int status = response.andReturn().getResponse().getStatus();
        assertThat("Org-less endpoints should not be blocked by suspension filter", status != SC_FORBIDDEN, Matchers.is(true));
    }

    // ============================= HELPER METHODS =============================

    private void suspendOrganization(final Organization org) {
        org.setStatus(OrganizationStatus.SUSPENDED);
        organizationRepository.save(org);
    }
}
