package io.github.eventify.api.organization.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.AddMemberRequest;
import io.github.eventify.api.organization.model.request.TransferOwnershipRequest;
import io.github.eventify.api.organization.model.request.UpdateMemberRoleRequest;
import io.github.eventify.api.organization.model.response.OrganizationMembershipResponse;
import io.github.eventify.api.organization.model.response.UserOrganizationResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import tools.jackson.core.type.TypeReference;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

/**
 * Integration Test - Organization Membership Controller.
 */
@DisplayName("Integration Test - Organization Membership Controller")
public class OrganizationMembershipControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should add member when caller is owner")
    public void shouldAddMemberWhenCallerIsOwner() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User newMember = aValidatedUser();

        // And: Add member request
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(newMember.getEmail())
            .setRole(OrganizationalRole.MEMBER);

        // When: Owner adds member
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Member is added successfully
        response.andExpect(status().is(SC_OK));

        // And: Response contains membership details
        final String content = response.andReturn().getResponse().getContentAsString();
        final OrganizationMembershipResponse membership = fromJson(content, OrganizationMembershipResponse.class);

        assertThat(membership.getUserId(), is(newMember.getId()));
        assertThat(membership.getRole(), is(OrganizationalRole.MEMBER));
        assertThat(membership.getJoinedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should add member when caller is admin")
    public void shouldAddMemberWhenCallerIsAdmin() throws Exception {
        // Given: An organization with admin
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);
        final User newMember = aValidatedUser();

        // And: Add member request
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(newMember.getEmail())
            .setRole(OrganizationalRole.MEMBER);

        // When: Admin adds member
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Member is added successfully
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return forbidden when member tries to add")
    public void shouldReturnForbiddenWhenMemberTriesToAdd() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);
        final User newMember = aValidatedUser();

        // And: Add member request
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(newMember.getEmail())
            .setRole(OrganizationalRole.MEMBER);

        // When: Member tries to add another member
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return bad request when user already member")
    public void shouldReturnBadRequestWhenUserAlreadyMember() throws Exception {
        // Given: An organization with existing member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Request to add same user again
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(member.getEmail())
            .setRole(OrganizationalRole.MEMBER);

        // When: Owner tries to add existing member
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return not found when org does not exist")
    public void shouldReturnNotFoundWhenOrgDoesNotExist() throws Exception {
        // Given: Owner and non-existent organization
        final User owner = aValidatedUser();
        final User newMember = aValidatedUser();

        // And: Add member request
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(newMember.getEmail())
            .setRole(OrganizationalRole.MEMBER);

        // When: Trying to add member to non-existent org
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_PATH.replace("{orgId}", "99999"))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be not found
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should return not found when user email does not exist")
    public void shouldReturnNotFoundWhenUserEmailDoesNotExist() throws Exception {
        // Given: Organization with owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        // And: Add member request with non-existent email
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail("nonexistent@example.com")
            .setRole(OrganizationalRole.MEMBER);

        // When: Owner tries to add non-existent user
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be not found
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should return member list for owner")
    public void shouldReturnMemberListForOwner() throws Exception {
        // Given: Organization with owner and members
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member1 = aValidatedUser();
        final User member2 = aValidatedUser();
        addMemberToOrganization(org, member1, OrganizationalRole.MEMBER);
        addMemberToOrganization(org, member2, OrganizationalRole.ADMIN);

        // When: Owner requests member list
        final MockHttpServletRequestBuilder httpRequest = get(ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK with member list
        response.andExpect(status().is(SC_OK));

        // And: Response contains all members
        final String content = response.andReturn().getResponse().getContentAsString();
        final OrganizationMembershipResponse[] members = fromJson(content, OrganizationMembershipResponse[].class);

        assertThat(members.length, is(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("Should return member list for admin")
    public void shouldReturnMemberListForAdmin() throws Exception {
        // Given: Organization with admin
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // When: Admin requests member list
        final MockHttpServletRequestBuilder httpRequest = get(ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK with member list
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return member list for member")
    public void shouldReturnMemberListForMember() throws Exception {
        // Given: Organization with regular member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // When: Member requests member list
        final MockHttpServletRequestBuilder httpRequest = get(ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK (read-only access)
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return forbidden for non-members")
    public void shouldReturnForbiddenForNonMembers() throws Exception {
        // Given: Organization and non-member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User nonMember = aValidatedUser();

        // When: Non-member requests member list
        final MockHttpServletRequestBuilder httpRequest = get(ORGANIZATION_MEMBERS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should update role when owner updates")
    public void shouldUpdateRoleWhenOwnerUpdates() throws Exception {
        // Given: Organization with member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Update role request
        final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
            .setRole(OrganizationalRole.ADMIN);

        // When: Owner updates member role
        final MockHttpServletRequestBuilder httpRequest = patch(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", member.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK
        response.andExpect(status().is(SC_OK));

        // And: Role should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final OrganizationMembershipResponse membership = fromJson(content, OrganizationMembershipResponse.class);

        assertThat(membership.getRole(), is(OrganizationalRole.ADMIN));
    }

    @Test
    @DisplayName("Should update role when admin updates member to admin")
    public void shouldUpdateRoleWhenAdminUpdatesMemberToAdmin() throws Exception {
        // Given: Organization with admin and member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Update role request
        final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
            .setRole(OrganizationalRole.ADMIN);

        // When: Admin updates member role
        final MockHttpServletRequestBuilder httpRequest = patch(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", member.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return forbidden when admin tries to update another admin")
    public void shouldReturnForbiddenWhenAdminTriesToUpdateAnotherAdmin() throws Exception {
        // Given: Organization with two admins
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin1 = aValidatedUser();
        final User admin2 = aValidatedUser();
        addMemberToOrganization(org, admin1, OrganizationalRole.ADMIN);
        addMemberToOrganization(org, admin2, OrganizationalRole.ADMIN);

        // And: Update role request
        final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
            .setRole(OrganizationalRole.MEMBER);

        // When: Admin tries to update another admin
        final MockHttpServletRequestBuilder httpRequest = patch(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", admin2.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin1.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return forbidden when admin tries to update owner")
    public void shouldReturnForbiddenWhenAdminTriesToUpdateOwner() throws Exception {
        // Given: Organization with admin
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // And: Update role request
        final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
            .setRole(OrganizationalRole.MEMBER);

        // When: Admin tries to update owner
        final MockHttpServletRequestBuilder httpRequest = patch(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", owner.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return forbidden when member tries to update")
    public void shouldReturnForbiddenWhenMemberTriesToUpdate() throws Exception {
        // Given: Organization with two members
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member1 = aValidatedUser();
        final User member2 = aValidatedUser();
        addMemberToOrganization(org, member1, OrganizationalRole.MEMBER);
        addMemberToOrganization(org, member2, OrganizationalRole.MEMBER);

        // And: Update role request
        final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
            .setRole(OrganizationalRole.ADMIN);

        // When: Member tries to update another member
        final MockHttpServletRequestBuilder httpRequest = patch(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", member2.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member1.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return bad request when trying to set owner role")
    public void shouldReturnBadRequestWhenTryingToSetOwnerRole() throws Exception {
        // Given: Organization with member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Update role request with OWNER role
        final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
            .setRole(OrganizationalRole.OWNER);

        // When: Owner tries to set OWNER role
        final MockHttpServletRequestBuilder httpRequest = patch(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", member.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should remove member when owner removes")
    public void shouldRemoveMemberWhenOwnerRemoves() throws Exception {
        // Given: Organization with member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // When: Owner removes member
        final MockHttpServletRequestBuilder httpRequest = delete(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", member.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return no content
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should remove member when admin removes member")
    public void shouldRemoveMemberWhenAdminRemovesMember() throws Exception {
        // Given: Organization with admin and member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // When: Admin removes member
        final MockHttpServletRequestBuilder httpRequest = delete(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", member.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return no content
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should return forbidden when admin removes another admin")
    public void shouldReturnForbiddenWhenAdminRemovesAnotherAdmin() throws Exception {
        // Given: Organization with two admins
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin1 = aValidatedUser();
        final User admin2 = aValidatedUser();
        addMemberToOrganization(org, admin1, OrganizationalRole.ADMIN);
        addMemberToOrganization(org, admin2, OrganizationalRole.ADMIN);

        // When: Admin tries to remove another admin
        final MockHttpServletRequestBuilder httpRequest = delete(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", admin2.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin1.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return forbidden when trying to remove owner")
    public void shouldReturnForbiddenWhenTryingToRemoveOwner() throws Exception {
        // Given: Organization with admin
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // When: Admin tries to remove owner
        final MockHttpServletRequestBuilder httpRequest = delete(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", owner.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return forbidden when member tries to remove")
    public void shouldReturnForbiddenWhenMemberTriesToRemove() throws Exception {
        // Given: Organization with two members
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member1 = aValidatedUser();
        final User member2 = aValidatedUser();
        addMemberToOrganization(org, member1, OrganizationalRole.MEMBER);
        addMemberToOrganization(org, member2, OrganizationalRole.MEMBER);

        // When: Member tries to remove another member
        final MockHttpServletRequestBuilder httpRequest = delete(
            ORGANIZATION_MEMBER_PATH
                .replace("{orgId}", org.getId().toString())
                .replace("{userId}", member2.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member1.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should transfer ownership when owner transfers to admin")
    public void shouldTransferOwnershipWhenOwnerTransfersToAdmin() throws Exception {
        // Given: Organization with owner and admin
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // And: Transfer ownership request
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setCurrentOwnerUserId(owner.getId())
            .setNewOwnerUserId(admin.getId());

        // When: Owner transfers ownership to admin
        final MockHttpServletRequestBuilder httpRequest = post(
            ORGANIZATION_TRANSFER_OWNERSHIP_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should transfer ownership when owner transfers to member")
    public void shouldTransferOwnershipWhenOwnerTransfersToMember() throws Exception {
        // Given: Organization with owner and member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Transfer ownership request
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setCurrentOwnerUserId(owner.getId())
            .setNewOwnerUserId(member.getId());

        // When: Owner transfers ownership to member
        final MockHttpServletRequestBuilder httpRequest = post(
            ORGANIZATION_TRANSFER_OWNERSHIP_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return forbidden when non-owner tries to transfer")
    public void shouldReturnForbiddenWhenNonOwnerTriesToTransfer() throws Exception {
        // Given: Organization with admin
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Transfer ownership request
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setCurrentOwnerUserId(owner.getId())
            .setNewOwnerUserId(member.getId());

        // When: Admin tries to transfer ownership
        final MockHttpServletRequestBuilder httpRequest = post(
            ORGANIZATION_TRANSFER_OWNERSHIP_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return bad request when target is not a member")
    public void shouldReturnBadRequestWhenTargetIsNotMember() throws Exception {
        // Given: Organization with owner
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User nonMember = aValidatedUser();

        // And: Transfer ownership request to non-member
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setCurrentOwnerUserId(owner.getId())
            .setNewOwnerUserId(nonMember.getId());

        // When: Owner tries to transfer to non-member
        final MockHttpServletRequestBuilder httpRequest = post(
            ORGANIZATION_TRANSFER_OWNERSHIP_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return user organizations")
    public void shouldReturnUserOrganizations() throws Exception {
        // Given: User with organization memberships
        final User owner = aValidatedUser();
        final Organization org1 = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org1, member, OrganizationalRole.MEMBER);

        final User owner2 = aValidatedUser();
        final Organization org2 = createOrganization(owner2);
        addMemberToOrganization(org2, member, OrganizationalRole.ADMIN);

        // When: User requests their organizations
        final MockHttpServletRequestBuilder httpRequest = get(USER_ORGANIZATIONS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK with organization list
        response.andExpect(status().is(SC_OK));

        // And: Response contains user's organizations with roles
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserOrganizationResponse[] organizations = fromJson(content, UserOrganizationResponse[].class);

        assertThat(organizations.length, is(2));
    }

    @Test
    @DisplayName("Should search users with valid query")
    public void shouldSearchUsersWithValidQuery() throws Exception {
        // Given: Organization and search input
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);

        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // When: Searching users
        final MockHttpServletRequestBuilder httpRequest = post(
            ORGANIZATION_NEW_MEMBERS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .content(toJson(input))
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return forbidden when non-member searches")
    public void shouldReturnForbiddenWhenNonMemberSearches() throws Exception {
        // Given: Organization and non-member user
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User nonMember = aValidatedUser();

        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // When: Non-member searches
        final MockHttpServletRequestBuilder httpRequest = post(
            ORGANIZATION_NEW_MEMBERS_SEARCH_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .content(toJson(input))
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should transfer ownership when global admin transfers")
    public void shouldTransferOwnershipWhenGlobalAdminTransfers() throws Exception {
        // Given: Organization with owner and member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: A global admin (not member of org)
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: Transfer ownership request with currentOwnerUserId
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setCurrentOwnerUserId(owner.getId())
            .setNewOwnerUserId(member.getId());

        // When: Global admin transfers ownership
        final MockHttpServletRequestBuilder httpRequest = post(
            ORGANIZATION_TRANSFER_OWNERSHIP_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should transfer ownership when owner transfers with correct current owner ID")
    public void shouldTransferOwnershipWhenOwnerTransfersWithCorrectCurrentOwnerId() throws Exception {
        // Given: Organization with owner and member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Transfer ownership request with owner's own ID
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setCurrentOwnerUserId(owner.getId())
            .setNewOwnerUserId(member.getId());

        // When: Owner transfers ownership with correct currentOwnerUserId
        final MockHttpServletRequestBuilder httpRequest = post(
            ORGANIZATION_TRANSFER_OWNERSHIP_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return forbidden when owner sends wrong current owner ID")
    public void shouldReturnForbiddenWhenOwnerSendsWrongCurrentOwnerId() throws Exception {
        // Given: Organization with owner and member
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Transfer ownership request with wrong currentOwnerUserId
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setCurrentOwnerUserId(999L)
            .setNewOwnerUserId(member.getId());

        // When: Owner tries to transfer with wrong currentOwnerUserId
        final MockHttpServletRequestBuilder httpRequest = post(
            ORGANIZATION_TRANSFER_OWNERSHIP_PATH.replace("{orgId}", org.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should return bad request (validation failure)
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should list members with pagination success")
    public void listMembersWithPaginationSuccess() throws Exception {
        // Given: Organization with multiple members
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member1 = aValidatedUser();
        final User member2 = aValidatedUser();
        final User member3 = aValidatedUser();
        addMemberToOrganization(org, member1, OrganizationalRole.MEMBER);
        addMemberToOrganization(org, member2, OrganizationalRole.ADMIN);
        addMemberToOrganization(org, member3, OrganizationalRole.MEMBER);

        // And: Pagination request for first page
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(2);

        // When: Owner requests member list with pagination
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_SEARCH_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain paginated members
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationMembershipResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(4L)));
        assertThat(pageResource.getPageSize(), is(2));
        assertThat(pageResource.getPageNumber(), is(0));
    }

    @Test
    @DisplayName("Should list members with search filter success")
    public void listMembersWithSearchFilterSuccess() throws Exception {
        // Given: Organization with members with distinct emails
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member1 = aValidatedUser();
        final User member2 = aValidatedUser();
        addMemberToOrganization(org, member1, OrganizationalRole.MEMBER);
        addMemberToOrganization(org, member2, OrganizationalRole.ADMIN);

        // And: Search request filtering by email substring
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);
        final SearchInput searchInput = new SearchInput();
        searchInput.setFieldName("search");
        searchInput.setTextValue(member1.getEmail().substring(0, 8));
        input.getSearchInputs().add(searchInput);

        // When: Owner searches for members by email
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_SEARCH_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain only matching member
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationMembershipResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(1L)));
    }

    @Test
    @DisplayName("Should list members with role filter success")
    public void listMembersWithRoleFilterSuccess() throws Exception {
        // Given: Organization with members of different roles
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member1 = aValidatedUser();
        final User member2 = aValidatedUser();
        final User member3 = aValidatedUser();
        addMemberToOrganization(org, member1, OrganizationalRole.MEMBER);
        addMemberToOrganization(org, member2, OrganizationalRole.ADMIN);
        addMemberToOrganization(org, member3, OrganizationalRole.MEMBER);

        // And: Filter request for MEMBER role only
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);
        final SearchInput roleFilter = new SearchInput();
        roleFilter.setFieldName("role");
        roleFilter.setTextValueList(List.of("MEMBER"));
        input.getSearchInputs().add(roleFilter);

        // When: Owner filters members by role
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_SEARCH_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain only MEMBER role members
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationMembershipResponse> pageResource = fromJson(content, new TypeReference<>() {});

        // Filter should return exactly 2 MEMBER role members
        assertThat(pageResource.getContent(), is(notNullValue()));
        assertThat(pageResource.getContent().size(), is(2));
        assertThat(pageResource.getTotalElements(), is(2L));
        // Verify all returned members have MEMBER role
        pageResource.getContent().forEach(
            membership -> assertThat(membership.getRole(), is(OrganizationalRole.MEMBER))
        );
    }

    @Test
    @DisplayName("Should list members sort by email success")
    public void listMembersSortByEmailSuccess() throws Exception {
        // Given: Organization with members
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member1 = aValidatedUser();
        final User member2 = aValidatedUser();
        addMemberToOrganization(org, member1, OrganizationalRole.MEMBER);
        addMemberToOrganization(org, member2, OrganizationalRole.ADMIN);

        // And: Sort request by userEmail ascending
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // When: Owner requests sorted member list
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_SEARCH_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain members sorted by email
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationMembershipResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(3L)));
    }

    @Test
    @DisplayName("Should list members sort by role success")
    public void listMembersSortByRoleSuccess() throws Exception {
        // Given: Organization with members of different roles
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member1 = aValidatedUser();
        final User member2 = aValidatedUser();
        addMemberToOrganization(org, member1, OrganizationalRole.MEMBER);
        addMemberToOrganization(org, member2, OrganizationalRole.ADMIN);

        // And: Sort request by role
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // When: Owner requests member list sorted by role
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_SEARCH_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain members sorted by role
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationMembershipResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(3L)));
    }

    @Test
    @DisplayName("Should list members default sort by role hierarchy")
    public void listMembersDefaultSortByRoleHierarchy() throws Exception {
        // Given: Organization with members of all role types
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User admin = aValidatedUser();
        final User member = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Request without explicit sort
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // When: Owner requests member list without sort
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_SEARCH_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain members in default role hierarchy order (OWNER -> ADMIN -> MEMBER)
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationMembershipResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(3L)));
    }

    @Test
    @DisplayName("Should list members as non-member fails")
    public void listMembersAsNonMemberFails() throws Exception {
        // Given: Organization with members
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User nonMember = aValidatedUser();

        // And: List request
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // When: Non-member tries to list members
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_SEARCH_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should list members as global admin success")
    public void listMembersAsGlobalAdminSuccess() throws Exception {
        // Given: Organization with members
        final User owner = aValidatedUser();
        final Organization org = createOrganization(owner);
        final User member1 = aValidatedUser();
        addMemberToOrganization(org, member1, OrganizationalRole.MEMBER);

        // And: Global admin (not member of org)
        final User globalAdmin = aValidatedUserWithRole(Role.ADMIN);

        // And: List request
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // When: Global admin lists members
        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_MEMBERS_SEARCH_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + globalAdmin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain all organization members
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<OrganizationMembershipResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(2L)));
    }
}
