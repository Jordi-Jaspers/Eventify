package io.github.eventify.api.notification.adapter.controller;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.request.CreateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.model.response.AdapterConfigResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static io.github.eventify.api.Paths.ORGANIZATION_ADAPTER_CONFIGS_PATH;
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

@DisplayName("Integration Test - Organization Adapter Config Controller")
public class OrganizationAdapterClientControllerTest extends IntegrationTest {

    // ========================= POST /v1/organization/{orgId}/adapter-configs =========================

    @Test
    @DisplayName("Should create org adapter config when user is OWNER")
    public void createOrgAdapterConfigSuccessAsOwner() throws Exception {
        // Given: An org owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: A valid request
        final CreateAdapterConfigRequest request = aValidSlackRequest();

        // When: Creating org config
        final ResultActions response = mockMvc.perform(
            post(ORGANIZATION_ADAPTER_CONFIGS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: Response is CREATED
        response.andExpect(status().is(SC_CREATED));
        final AdapterConfigResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdapterConfigResponse.class
        );
        assertThat(body.getId(), is(notNullValue()));
        assertThat(body.getAdapterType(), is(AdapterType.SLACK));
    }

    @Test
    @DisplayName("Should create org adapter config when user is ADMIN")
    public void createOrgAdapterConfigSuccessAsAdmin() throws Exception {
        // Given: An org with owner and admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User adminMember = aValidatedUser();
        addMemberToOrganization(org, adminMember, OrganizationalRole.ADMIN);

        // And: A valid request
        final CreateAdapterConfigRequest request = aValidSlackRequest();

        // When: Admin creates org config
        final ResultActions response = mockMvc.perform(
            post(ORGANIZATION_ADAPTER_CONFIGS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + adminMember.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: Response is CREATED
        response.andExpect(status().is(SC_CREATED));
    }

    @Test
    @DisplayName("Should return 403 when MEMBER tries to create org adapter config")
    public void createOrgAdapterConfigForbiddenForMember() throws Exception {
        // Given: An org member (not OWNER/ADMIN)
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: A valid request
        final CreateAdapterConfigRequest request = aValidSlackRequest();

        // When: Member attempts to create org config
        final ResultActions response = mockMvc.perform(
            post(ORGANIZATION_ADAPTER_CONFIGS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: Forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= GET /v1/organization/{orgId}/adapter-configs =========================

    @Test
    @DisplayName("Should list org adapter configs when user is OWNER")
    public void listOrgAdapterConfigsSuccessAsOwner() throws Exception {
        // Given: An org owner with one config
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // Create config first
        mockMvc.perform(
            post(ORGANIZATION_ADAPTER_CONFIGS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(aValidSlackRequest()))
        );

        // When: Listing
        final ResultActions response = mockMvc.perform(
            get(ORGANIZATION_ADAPTER_CONFIGS_PATH.replace("{orgId}", org.getId().toString()))
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
        );

        // Then: Response is OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return 403 when MEMBER tries to list org adapter configs")
    public void listOrgAdapterConfigsForbiddenForMember() throws Exception {
        // Given: An org member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // When: Member tries to list
        final ResultActions response = mockMvc.perform(
            get(ORGANIZATION_ADAPTER_CONFIGS_PATH.replace("{orgId}", org.getId().toString()))
                .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
        );

        // Then: Forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= FACTORY METHODS =========================

    private static CreateAdapterConfigRequest aValidSlackRequest() {
        return new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.SLACK)
            .setLabel("Team Slack")
            .setConfig(Map.of("webhookUrl", "https://example.com/webhook/services/T00/B00/XXXXXXXX"))
            .setEnabled(true);
    }
}
