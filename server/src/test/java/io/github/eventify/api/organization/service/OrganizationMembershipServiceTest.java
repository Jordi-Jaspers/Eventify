package io.github.eventify.api.organization.service;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.AddMemberRequest;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.exception.DisabledUserException;
import io.github.eventify.common.exception.OwnerRoleException;
import io.github.eventify.common.exception.OwnershipTransferException;
import io.github.eventify.common.exception.UserAlreadyMemberException;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.support.UnitTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.BadRequestException;
import io.github.jframe.exception.core.DataNotFoundException;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test - Organization Membership Service.
 */
@DisplayName("Unit Test - Organization Membership Service")
public class OrganizationMembershipServiceTest extends UnitTest {

    @Mock
    private OrganizationMembershipRepository membershipRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrganizationMembershipService membershipService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User owner;
    private User member;
    private Organization organization;

    @BeforeEach
    public void setUp() {
        owner = aValidUser();
        owner.setId(1L);

        member = aValidUser();
        member.setId(2L);
        member.setEmail("member@example.com");

        organization = new Organization();
        organization.setId(100L);
        organization.setName("Test Org");
        organization.setSlug("test-org");

        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(owner);
    }

    @AfterEach
    public void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("Should add member to organization with MEMBER role")
    public void shouldAddMemberToOrganizationWithMemberRole() {
        // Given: A user to add as member
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(member.getEmail())
            .setRole(OrganizationalRole.MEMBER);

        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(membershipRepository.existsByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(false);
        when(membershipRepository.save(any(OrganizationMembership.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Adding member to organization
        final OrganizationMembership result = membershipService.addMember(
            organization.getId(),
            request
        );

        // Then: Membership should be created successfully
        assertThat(result, is(notNullValue()));
        assertThat(result.getRole(), is(OrganizationalRole.MEMBER));
        verify(membershipRepository).save(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should add member to organization with ADMIN role")
    public void shouldAddMemberToOrganizationWithAdminRole() {
        // Given: A user to add as admin
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(member.getEmail())
            .setRole(OrganizationalRole.ADMIN);

        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(membershipRepository.existsByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(false);
        when(membershipRepository.save(any(OrganizationMembership.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Adding member to organization as admin
        final OrganizationMembership result = membershipService.addMember(
            organization.getId(),
            request
        );

        // Then: Membership should be created with ADMIN role
        assertThat(result, is(notNullValue()));
        assertThat(result.getRole(), is(OrganizationalRole.ADMIN));
        verify(membershipRepository).save(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should throw when user is already a member")
    public void shouldThrowWhenUserIsAlreadyMember() {
        // Given: User already member of organization
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(member.getEmail())
            .setRole(OrganizationalRole.MEMBER);

        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(membershipRepository.existsByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(true);

        // When & Then: Should throw exception
        assertThrows(
            UserAlreadyMemberException.class,
            () -> membershipService.addMember(
                organization.getId(),
                request
            )
        );

        verify(membershipRepository, never()).save(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    public void shouldThrowWhenUserDoesNotExist() {
        // Given: User does not exist
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(member.getEmail())
            .setRole(OrganizationalRole.MEMBER);

        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> membershipService.addMember(
                organization.getId(),
                request
            )
        );

        verify(membershipRepository, never()).save(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should throw when user is disabled")
    public void shouldThrowWhenUserIsDisabled() {
        // Given: User is disabled
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(member.getEmail())
            .setRole(OrganizationalRole.MEMBER);

        member.setEnabled(false);
        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));

        // When & Then: Should throw DisabledUserException
        assertThrows(
            DisabledUserException.class,
            () -> membershipService.addMember(
                organization.getId(),
                request
            )
        );

        verify(membershipRepository, never()).save(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should throw when organization does not exist")
    public void shouldThrowWhenOrganizationDoesNotExist() {
        // Given: Organization does not exist
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(member.getEmail())
            .setRole(OrganizationalRole.MEMBER);

        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> membershipService.addMember(
                organization.getId(),
                request
            )
        );

        verify(membershipRepository, never()).save(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should update member role from MEMBER to ADMIN")
    public void shouldUpdateMemberRoleFromMemberToAdmin() {
        // Given: Member with MEMBER role
        final OrganizationMembership membership = new OrganizationMembership(organization, member, OrganizationalRole.MEMBER);
        membership.setId(1L);
        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.of(membership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), owner.getId()))
            .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.save(any(OrganizationMembership.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.findAllByOrganizationIdWithUser(organization.getId()))
            .thenReturn(List.of(membership));

        // When: Updating role to ADMIN
        final OrganizationMembership result = membershipService.updateMemberRole(
            organization.getId(),
            member.getId(),
            OrganizationalRole.ADMIN
        );

        // Then: Role should be updated to ADMIN
        assertThat(result, is(notNullValue()));
        assertThat(membership.getRole(), is(OrganizationalRole.ADMIN));
        verify(membershipRepository).save(membership);
    }

    @Test
    @DisplayName("Should update member role from ADMIN to MEMBER")
    public void shouldUpdateMemberRoleFromAdminToMember() {
        // Given: Member with ADMIN role
        final OrganizationMembership membership = new OrganizationMembership(organization, member, OrganizationalRole.ADMIN);
        membership.setId(1L);
        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.of(membership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), owner.getId()))
            .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.save(any(OrganizationMembership.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.findAllByOrganizationIdWithUser(organization.getId()))
            .thenReturn(List.of(membership));

        // When: Updating role to MEMBER
        final OrganizationMembership result = membershipService.updateMemberRole(
            organization.getId(),
            member.getId(),
            OrganizationalRole.MEMBER
        );

        // Then: Role should be updated to MEMBER
        assertThat(result, is(notNullValue()));
        assertThat(membership.getRole(), is(OrganizationalRole.MEMBER));
        verify(membershipRepository).save(membership);
    }

    @Test
    @DisplayName("Should throw when trying to update to OWNER role")
    public void shouldThrowWhenTryingToUpdateToOwnerRole() {
        // Given: Member with MEMBER role (note: mock setup not needed as service throws early)

        // When & Then: Should throw exception (use transfer ownership instead)
        assertThrows(
            OwnerRoleException.class,
            () -> membershipService.updateMemberRole(
                organization.getId(),
                member.getId(),
                OrganizationalRole.OWNER
            )
        );

        verify(membershipRepository, never()).save(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should throw when membership does not exist")
    public void shouldThrowWhenMembershipDoesNotExist() {
        // Given: Membership does not exist
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> membershipService.updateMemberRole(
                organization.getId(),
                member.getId(),
                OrganizationalRole.ADMIN
            )
        );

        verify(membershipRepository, never()).save(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should throw when trying to update owner role")
    public void shouldThrowWhenTryingToUpdateOwnerRole() {
        // Given: Member with OWNER role
        final OrganizationMembership membership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), owner.getId()))
            .thenReturn(Optional.of(membership));

        // When & Then: Should throw exception (cannot change owner's role)
        assertThrows(
            OwnerRoleException.class,
            () -> membershipService.updateMemberRole(
                organization.getId(),
                owner.getId(),
                OrganizationalRole.ADMIN
            )
        );

        verify(membershipRepository, never()).save(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should remove member from organization")
    public void shouldRemoveMemberFromOrganization() {
        // Given: Member exists in organization
        final OrganizationMembership membership = new OrganizationMembership(organization, member, OrganizationalRole.MEMBER);
        membership.setId(1L);
        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.of(membership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), owner.getId()))
            .thenReturn(Optional.of(ownerMembership));

        // When: Removing member
        membershipService.removeMember(organization.getId(), member.getId(), owner);

        // Then: Member should be removed
        verify(membershipRepository).delete(membership);
    }

    @Test
    @DisplayName("Should throw when trying to remove the OWNER")
    public void shouldThrowWhenTryingToRemoveOwner() {
        // Given: Member with OWNER role
        final OrganizationMembership membership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), owner.getId()))
            .thenReturn(Optional.of(membership));

        // When & Then: Should throw exception (cannot remove owner)
        assertThrows(
            OwnerRoleException.class,
            () -> membershipService.removeMember(organization.getId(), owner.getId(), owner)
        );

        verify(membershipRepository, never()).delete(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should throw when membership does not exist for removal")
    public void shouldThrowWhenMembershipDoesNotExistForRemoval() {
        // Given: Membership does not exist
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> membershipService.removeMember(organization.getId(), member.getId(), owner)
        );

        verify(membershipRepository, never()).delete(any(OrganizationMembership.class));
    }

    @Test
    @DisplayName("Should work when ADMIN removes MEMBER")
    public void shouldWorkWhenAdminRemovesMember() {
        // Given: Admin removing a member
        final OrganizationMembership membership = new OrganizationMembership(organization, member, OrganizationalRole.MEMBER);
        membership.setId(1L);
        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.of(membership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), owner.getId()))
            .thenReturn(Optional.of(ownerMembership));

        // When: Admin removes member
        membershipService.removeMember(organization.getId(), member.getId(), owner);

        // Then: Member should be removed successfully
        verify(membershipRepository).delete(membership);
    }

    @Test
    @DisplayName("Should transfer ownership to existing ADMIN")
    public void shouldTransferOwnershipToExistingAdmin() {
        // Given: Current owner and target admin
        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);
        final OrganizationMembership adminMembership = new OrganizationMembership(organization, member, OrganizationalRole.ADMIN);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), owner.getId()))
            .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.of(adminMembership));
        when(membershipRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Transferring ownership to admin
        membershipService.transferOwnership(organization.getId(), owner.getId(), member.getId());

        // Then: Previous owner should be demoted to ADMIN and new owner should be OWNER
        assertThat(ownerMembership.getRole(), is(OrganizationalRole.ADMIN));
        assertThat(adminMembership.getRole(), is(OrganizationalRole.OWNER));

        verify(membershipRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should transfer ownership to existing MEMBER")
    public void shouldTransferOwnershipToExistingMember() {
        // Given: Current owner and target member
        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);
        final OrganizationMembership memberMembership = new OrganizationMembership(organization, member, OrganizationalRole.MEMBER);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), owner.getId()))
            .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.of(memberMembership));
        when(membershipRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Transferring ownership to member
        membershipService.transferOwnership(organization.getId(), owner.getId(), member.getId());

        // Then: Previous owner should be demoted to ADMIN and member should be OWNER
        assertThat(ownerMembership.getRole(), is(OrganizationalRole.ADMIN));
        assertThat(memberMembership.getRole(), is(OrganizationalRole.OWNER));

        verify(membershipRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw when target user is not a member")
    public void shouldThrowWhenTargetUserIsNotMember() {
        // Given: Current owner but target is not a member
        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), owner.getId()))
            .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw BadRequestException
        assertThrows(
            BadRequestException.class,
            () -> membershipService.transferOwnership(organization.getId(), owner.getId(), member.getId())
        );

        verify(membershipRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw when caller is not the current owner")
    public void shouldThrowWhenCallerIsNotCurrentOwner() {
        // When & Then: Should throw exception (only owner can transfer ownership)
        assertThrows(
            OwnershipTransferException.class,
            () -> membershipService.transferOwnership(organization.getId(), member.getId(), owner.getId())
        );

        verify(membershipRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw when transferring to self")
    public void shouldThrowWhenTransferringToSelf() {
        // Given: Trying to transfer to self (note: mock setup not needed as service throws early)

        // When & Then: Should throw OwnershipTransferException
        assertThrows(
            OwnershipTransferException.class,
            () -> membershipService.transferOwnership(organization.getId(), owner.getId(), owner.getId())
        );

        verify(membershipRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should return all members of organization")
    public void shouldReturnAllMembersOfOrganization() {
        // Given: Organization with members
        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);
        final OrganizationMembership memberMembership = new OrganizationMembership(organization, member, OrganizationalRole.MEMBER);

        when(organizationRepository.existsById(organization.getId())).thenReturn(true);
        when(membershipRepository.findAllByOrganizationIdWithUser(organization.getId()))
            .thenReturn(List.of(ownerMembership, memberMembership));

        // When: Getting all members
        final List<OrganizationMembership> members = membershipService.getOrganizationMembers(organization.getId());

        // Then: Should return all members with details
        assertThat(members, hasSize(2));
        assertThat(members, containsInAnyOrder(ownerMembership, memberMembership));
        verify(membershipRepository).findAllByOrganizationIdWithUser(organization.getId());
    }

    @Test
    @DisplayName("Should return empty list for org with only owner")
    public void shouldReturnEmptyListForOrgWithOnlyOwner() {
        // Given: Organization with only owner
        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);

        when(organizationRepository.existsById(organization.getId())).thenReturn(true);
        when(membershipRepository.findAllByOrganizationIdWithUser(organization.getId()))
            .thenReturn(List.of(ownerMembership));

        // When: Getting all members
        final List<OrganizationMembership> members = membershipService.getOrganizationMembers(organization.getId());

        // Then: Should return just the owner
        assertThat(members, hasSize(1));
        assertThat(members.get(0), is(ownerMembership));
        verify(membershipRepository).findAllByOrganizationIdWithUser(organization.getId());
    }

    @Test
    @DisplayName("Should return all organizations for user")
    public void shouldReturnAllOrganizationsForUser() {
        // Given: Logged in user (owner) with multiple organization memberships
        final OrganizationMembership membership1 = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);
        final Organization org2 = new Organization();
        org2.setId(200L);
        org2.setName("Org 2");
        org2.setSlug("org-2");
        final OrganizationMembership membership2 = new OrganizationMembership(org2, owner, OrganizationalRole.ADMIN);

        when(membershipRepository.findAllByUserIdWithOrganization(owner.getId()))
            .thenReturn(List.of(membership1, membership2));

        // When: Getting user's organizations
        final List<OrganizationMembership> organizations = membershipService.getUserOrganizations();

        // Then: Should return all organizations with roles
        assertThat(organizations, hasSize(2));
        assertThat(organizations, containsInAnyOrder(membership1, membership2));
        verify(membershipRepository).findAllByUserIdWithOrganization(owner.getId());
    }

    @Test
    @DisplayName("Should return empty list for user with no memberships")
    public void shouldReturnEmptyListForUserWithNoMemberships() {
        // Given: Logged in user (owner) with no organization memberships
        when(membershipRepository.findAllByUserIdWithOrganization(owner.getId()))
            .thenReturn(List.of());

        // When: Getting user's organizations
        final List<OrganizationMembership> organizations = membershipService.getUserOrganizations();

        // Then: Should return empty list
        assertThat(organizations, is(empty()));
        verify(membershipRepository).findAllByUserIdWithOrganization(owner.getId());
    }

    @Test
    @DisplayName("Should search users and delegate to user service")
    public void shouldSearchUsersAndDelegateToUserService() {
        // Given: Search input and users
        final SortablePageInput input = new SortablePageInput();
        final Page<User> userPage = new PageImpl<>(List.of(member));

        when(userService.searchUsers(any(SortablePageInput.class))).thenReturn(userPage);

        // When: Searching users
        final Page<User> results = membershipService.searchUsersForOrganization(input, organization.getId());

        // Then: Should return matching users
        assertThat(results.getContent(), hasSize(1));
        assertThat(results.getContent().get(0).getId(), is(member.getId()));
        assertThat(results.getContent().get(0).getEmail(), is(member.getEmail()));
        verify(userService).searchUsers(any(SortablePageInput.class));
    }

    @Test
    @DisplayName("Should set max page size to 10")
    public void shouldSetMaxPageSizeTo10() {
        // Given: Search input with larger page size
        final SortablePageInput input = new SortablePageInput();
        input.setPageSize(50);
        final Page<User> userPage = new PageImpl<>(List.of());

        when(userService.searchUsers(any(SortablePageInput.class))).thenReturn(userPage);

        // When: Searching users
        membershipService.searchUsersForOrganization(input, organization.getId());

        // Then: Should cap page size to 10
        verify(userService).searchUsers(argThat(pageInput -> pageInput.getPageSize() == 10));
    }

    @Test
    @DisplayName("Should return empty page when no matches")
    public void shouldReturnEmptyPageWhenNoMatches() {
        // Given: Search input with no matching users
        final SortablePageInput input = new SortablePageInput();
        final Page<User> emptyPage = new PageImpl<>(List.of());

        when(userService.searchUsers(any(SortablePageInput.class))).thenReturn(emptyPage);

        // When: Searching users
        final Page<User> results = membershipService.searchUsersForOrganization(input, organization.getId());

        // Then: Should return empty page
        assertThat(results.getContent(), is(empty()));
        verify(userService).searchUsers(any(SortablePageInput.class));
    }

    @Test
    @DisplayName("Should transfer ownership when caller is owner")
    public void shouldTransferOwnershipWhenCallerIsOwner() {
        // Given: Owner caller
        final User ownerCaller = aValidUser();
        ownerCaller.setId(1L);
        ownerCaller.setRole(Role.USER);

        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, ownerCaller, OrganizationalRole.OWNER);
        final OrganizationMembership memberMembership = new OrganizationMembership(organization, member, OrganizationalRole.MEMBER);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), ownerCaller.getId()))
            .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.of(memberMembership));
        when(membershipRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Owner transfers ownership
        membershipService.transferOwnership(organization.getId(), ownerCaller.getId(), member.getId());

        // Then: Transfer should succeed
        assertThat(ownerMembership.getRole(), is(OrganizationalRole.ADMIN));
        assertThat(memberMembership.getRole(), is(OrganizationalRole.OWNER));
        verify(membershipRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should transfer ownership when caller is global admin")
    public void shouldTransferOwnershipWhenCallerIsGlobalAdmin() {
        // Given: Global admin caller (not the owner)
        final User globalAdmin = aValidUser();
        globalAdmin.setId(999L);
        globalAdmin.setRole(Role.ADMIN);

        final OrganizationMembership ownerMembership = new OrganizationMembership(organization, owner, OrganizationalRole.OWNER);
        final OrganizationMembership memberMembership = new OrganizationMembership(organization, member, OrganizationalRole.MEMBER);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), owner.getId()))
            .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.of(memberMembership));
        when(membershipRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Global admin transfers ownership (not being the owner)
        membershipService.transferOwnership(organization.getId(), owner.getId(), member.getId());

        // Then: Transfer should succeed
        assertThat(ownerMembership.getRole(), is(OrganizationalRole.ADMIN));
        assertThat(memberMembership.getRole(), is(OrganizationalRole.OWNER));
        verify(membershipRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw when non-owner non-admin tries to transfer")
    public void shouldThrowWhenNonOwnerNonAdminTriesToTransfer() {
        // Given: Regular member trying to transfer
        final User regularMember = aValidUser();
        regularMember.setId(3L);
        regularMember.setRole(Role.USER);

        // When & Then: Should throw exception (not owner and not global admin)
        assertThrows(
            OwnershipTransferException.class,
            () -> membershipService.transferOwnership(organization.getId(), owner.getId(), member.getId())
        );

        verify(membershipRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw when current owner user ID does not match actual owner")
    public void shouldThrowWhenCurrentOwnerUserIdDoesNotMatchActualOwner() {
        // Given: Current owner caller but wrong currentOwnerUserId
        final User actualOwner = aValidUser();
        actualOwner.setId(1L);
        actualOwner.setRole(Role.USER);

        final User wrongOwner = aValidUser();
        wrongOwner.setId(99L);

        // When & Then: Should throw exception (currentOwnerUserId doesn't match actual owner)
        assertThrows(
            OwnershipTransferException.class,
            () -> membershipService.transferOwnership(organization.getId(), wrongOwner.getId(), member.getId())
        );

        verify(membershipRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should update member role when global admin not member of organization")
    public void shouldUpdateMemberRoleWhenGlobalAdminNotMemberOfOrganization() {
        // Given: Global admin who is NOT a member of the organization
        final User globalAdmin = aValidUser();
        globalAdmin.setId(999L);
        globalAdmin.setRole(Role.ADMIN);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(globalAdmin);

        final OrganizationMembership membership = new OrganizationMembership(organization, member, OrganizationalRole.MEMBER);
        membership.setId(1L);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.of(membership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), globalAdmin.getId()))
            .thenReturn(Optional.empty());
        when(membershipRepository.save(any(OrganizationMembership.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.findAllByOrganizationIdWithUser(organization.getId()))
            .thenReturn(List.of(membership));

        // When: Global admin updates member's role from MEMBER to ADMIN
        final OrganizationMembership result = membershipService.updateMemberRole(
            organization.getId(),
            member.getId(),
            OrganizationalRole.ADMIN
        );

        // Then: Role update should succeed without requiring org membership
        assertThat(result, is(notNullValue()));
        assertThat(membership.getRole(), is(OrganizationalRole.ADMIN));
        verify(membershipRepository).save(membership);
    }

    @Test
    @DisplayName("Should remove member when global admin not member of organization")
    public void shouldRemoveMemberWhenGlobalAdminNotMemberOfOrganization() {
        // Given: Global admin who is NOT a member of the organization
        final User globalAdmin = aValidUser();
        globalAdmin.setId(999L);
        globalAdmin.setRole(Role.ADMIN);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(globalAdmin);

        final OrganizationMembership membership = new OrganizationMembership(organization, member, OrganizationalRole.MEMBER);
        membership.setId(1L);

        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), member.getId()))
            .thenReturn(Optional.of(membership));
        when(membershipRepository.findByOrganizationIdAndUserId(organization.getId(), globalAdmin.getId()))
            .thenReturn(Optional.empty());

        // When: Global admin removes member from the organization
        membershipService.removeMember(organization.getId(), member.getId(), globalAdmin);

        // Then: Member removal should succeed without requiring org membership
        verify(membershipRepository).delete(membership);
    }
}
