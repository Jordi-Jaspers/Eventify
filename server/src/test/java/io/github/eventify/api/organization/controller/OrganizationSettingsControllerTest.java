package io.github.eventify.api.organization.controller;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.request.UpdateRetentionRequest;
import io.github.eventify.api.user.model.response.RetentionSettingsResponse;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ORGANIZATION_RETENTION_SETTINGS_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Organization Settings Controller")
public class OrganizationSettingsControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return default retention days for organization owner")
    public void getRetentionSettingsAsOwner() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Owner requests retention settings
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain default retention days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(90));
    }

    @Test
    @DisplayName("Should return retention days for organization admin")
    public void getRetentionSettingsAsAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // When: Admin requests retention settings
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain default retention days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(90));
    }

    @Test
    @DisplayName("Should return forbidden when member tries to access retention settings")
    public void getRetentionSettingsAsMemberForbidden() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // When: Member requests retention settings
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should update retention days as owner with valid value")
    public void updateRetentionSettingsAsOwnerSuccess() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Valid retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(365);

        // When: Owner updates retention settings
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain updated retention days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(365));
    }

    @Test
    @DisplayName("Should update retention days as admin with valid value")
    public void updateRetentionSettingsAsAdminSuccess() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // And: Valid retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(180);

        // When: Admin updates retention settings
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain updated retention days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(180));
    }

    @Test
    @DisplayName("Should return forbidden when member tries to update retention settings")
    public void updateRetentionSettingsAsMemberForbidden() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Valid retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(365);

        // When: Member tries to update retention settings
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should update organization retention to 90 days")
    public void updateOrganizationRetentionTo90Days() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Retention request for 90 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(90);

        // When: Updating retention settings
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Retention should be 90 days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(90));
    }

    @Test
    @DisplayName("Should update organization retention to 730 days")
    public void updateOrganizationRetentionTo730Days() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Retention request for 730 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(730);

        // When: Updating retention settings
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Retention should be 730 days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(730));
    }

    @Test
    @DisplayName("Should update organization retention to 1095 days")
    public void updateOrganizationRetentionTo1095Days() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Retention request for 1095 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(1095);

        // When: Updating retention settings
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Retention should be 1095 days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(1095));
    }

    @Test
    @DisplayName("Should update organization retention to 1825 days")
    public void updateOrganizationRetentionTo1825Days() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Retention request for 1825 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(1825);

        // When: Updating retention settings
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Retention should be 1825 days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(1825));
    }

    @Test
    @DisplayName("Should return bad request when retention days is invalid value 100")
    public void updateOrganizationRetentionInvalidValue100() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Invalid retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(100);

        // When: Updating with invalid value
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when retention days is invalid value 500")
    public void updateOrganizationRetentionInvalidValue500() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Invalid retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(500);

        // When: Updating with invalid value
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when retention days is negative")
    public void updateOrganizationRetentionNegativeValue() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Negative retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(-1);

        // When: Updating with negative value
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when retention days is zero")
    public void updateOrganizationRetentionZeroValue() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Zero retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(0);

        // When: Updating with zero value
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when retention days is null")
    public void updateOrganizationRetentionNullValue() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Null retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(null);

        // When: Updating with null value
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return unauthorized when not authenticated")
    public void getOrganizationRetentionUnauthorized() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting retention without auth
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return unauthorized when updating without authentication")
    public void updateOrganizationRetentionUnauthorized() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Valid retention request but no authentication
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(365);

        // When: Updating without auth
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return forbidden when non-member tries to access retention settings")
    public void getNonMemberRetentionForbidden() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Another user who is not a member
        final User nonMember = aValidatedUser();

        // When: Non-member requests retention settings
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return forbidden when non-member tries to update retention settings")
    public void updateNonMemberRetentionForbidden() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Another user who is not a member
        final User nonMember = aValidatedUser();

        // And: Valid retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(365);

        // When: Non-member tries to update retention settings
        final MockHttpServletRequestBuilder updateRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return updated retention when owner checks after update")
    public void getOrganizationRetentionAfterUpdate() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Owner has updated retention to 1095 days
        final UpdateRetentionRequest updateRequest = new UpdateRetentionRequest()
            .setRetentionDays(1095);

        final MockHttpServletRequestBuilder updateHttpRequest = put(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(updateRequest));

        mockMvc.perform(updateHttpRequest);

        // When: Requesting retention settings
        final MockHttpServletRequestBuilder getRequest = get(
            ORGANIZATION_RETENTION_SETTINGS_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should show updated retention
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(1095));
    }
}
