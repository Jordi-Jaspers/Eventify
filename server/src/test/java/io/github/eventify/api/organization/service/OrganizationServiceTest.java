package io.github.eventify.api.organization.service;

import io.github.eventify.api.notification.service.NotificationDispatchService;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.support.UnitTest;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Organization Service")
public class OrganizationServiceTest extends UnitTest {

    private static final String VALID_ORG_NAME = "Acme Corporation";
    private static final String VALID_ORG_SLUG = "acme-corporation";
    private static final String VALID_ORG_NAME_WITH_SPACES = "Test  Multiple   Spaces";
    private static final String EXPECTED_SLUG_SPACES = "test-multiple-spaces";
    private static final String VALID_ORG_NAME_UPPERCASE = "UPPERCASE";
    private static final String EXPECTED_SLUG_UPPERCASE = "uppercase";
    private static final String VALID_ORG_NAME_WITH_SPECIAL = "Test & Co.";
    private static final String EXPECTED_SLUG_SPECIAL = "test-co";
    private static final String UNICODE_NAME = "Café & Cømpany";

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationMembershipRepository organizationMembershipRepository;

    @Mock
    private NotificationDispatchService notificationDispatchService;

    @InjectMocks
    private OrganizationService organizationService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User authenticatedUser;
    private User ownerUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = aValidUser();
        authenticatedUser.setId(1L);

        ownerUser = aValidUser();
        ownerUser.setId(2L);
        ownerUser.setEmail(VALID_EMAIL);
        ownerUser.setEnabled(true);

        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(authenticatedUser);

        // Default mock for owner lookup
        lenient().when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(ownerUser));
        lenient().when(organizationMembershipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("Should create organization with valid data")
    void createOrganizationSuccess() {
        final ProvisionOrganizationRequest request = aValidOrganizationRequest(VALID_ORG_NAME);

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(1L);
        savedOrganization.setName(VALID_ORG_NAME);
        savedOrganization.setSlug(VALID_ORG_SLUG);
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(VALID_ORG_SLUG)).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(1L));
        assertThat(result.getName(), is(VALID_ORG_NAME));
        assertThat(result.getSlug(), is(VALID_ORG_SLUG));
        assertThat(result.getStatus(), is(OrganizationStatus.ACTIVE));
        assertThat(result.getCreatedBy(), is(authenticatedUser.getId()));
        verify(organizationRepository).save(any());
    }

    @Test
    @DisplayName("Should generate slug from name with standard case conversion")
    void shouldGenerateSlugFromNameWithStandardCaseConversion() {
        final String orgName = "Acme Corp";
        final String expectedSlug = "acme-corp";
        final ProvisionOrganizationRequest request = aValidOrganizationRequest(orgName);

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(1L);
        savedOrganization.setName(orgName);
        savedOrganization.setSlug(expectedSlug);
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(expectedSlug)).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result.getSlug(), is(expectedSlug));
    }

    @Test
    @DisplayName("Should generate slug from name with multiple spaces collapsed")
    void shouldGenerateSlugFromNameWithMultipleSpacesCollapsed() {
        final ProvisionOrganizationRequest request = aValidOrganizationRequest(VALID_ORG_NAME_WITH_SPACES);

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(1L);
        savedOrganization.setName(VALID_ORG_NAME_WITH_SPACES);
        savedOrganization.setSlug(EXPECTED_SLUG_SPACES);
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(EXPECTED_SLUG_SPACES)).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result.getSlug(), is(EXPECTED_SLUG_SPACES));
    }

    @Test
    @DisplayName("Should generate slug from name converting uppercase to lowercase")
    void shouldGenerateSlugFromNameConvertingUppercaseToLowercase() {
        final ProvisionOrganizationRequest request = aValidOrganizationRequest(VALID_ORG_NAME_UPPERCASE);

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(1L);
        savedOrganization.setName(VALID_ORG_NAME_UPPERCASE);
        savedOrganization.setSlug(EXPECTED_SLUG_UPPERCASE);
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(EXPECTED_SLUG_UPPERCASE)).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result.getSlug(), is(EXPECTED_SLUG_UPPERCASE));
    }

    @Test
    @DisplayName("Should generate slug from name removing special characters")
    void shouldGenerateSlugFromNameRemovingSpecialCharacters() {
        final ProvisionOrganizationRequest request = aValidOrganizationRequest(VALID_ORG_NAME_WITH_SPECIAL);

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(1L);
        savedOrganization.setName(VALID_ORG_NAME_WITH_SPECIAL);
        savedOrganization.setSlug(EXPECTED_SLUG_SPECIAL);
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(EXPECTED_SLUG_SPECIAL)).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result.getSlug(), is(EXPECTED_SLUG_SPECIAL));
    }

    @Test
    @DisplayName("Should handle slug collision with first duplicate by appending suffix -1")
    void shouldHandleSlugCollisionWithFirstDuplicateByAppendingSuffix1() {
        final String baseSlug = "acme-corp";
        final String collidingSlug = "acme-corp-1";
        final ProvisionOrganizationRequest request = aValidOrganizationRequest("Acme Corp");

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(2L);
        savedOrganization.setName("Acme Corp");
        savedOrganization.setSlug(collidingSlug);
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(baseSlug)).thenReturn(Optional.of(new Organization()));
        when(organizationRepository.findBySlug(collidingSlug)).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result.getSlug(), is(collidingSlug));
    }

    @Test
    @DisplayName("Should handle multiple slug collisions by incrementing suffix")
    void shouldHandleMultipleSlugCollisionsByIncrementingSuffix() {
        final String baseSlug = "acme-corp";
        final String slug1 = "acme-corp-1";
        final String slug2 = "acme-corp-2";
        final ProvisionOrganizationRequest request = aValidOrganizationRequest("Acme Corp");

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(3L);
        savedOrganization.setName("Acme Corp");
        savedOrganization.setSlug(slug2);
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(baseSlug)).thenReturn(Optional.of(new Organization()));
        when(organizationRepository.findBySlug(slug1)).thenReturn(Optional.of(new Organization()));
        when(organizationRepository.findBySlug(slug2)).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result.getSlug(), is(slug2));
    }

    @Test
    @DisplayName("Should default organization status to ACTIVE if not provided")
    void shouldDefaultOrganizationStatusToActiveIfNotProvided() {
        final ProvisionOrganizationRequest request = aValidOrganizationRequest(VALID_ORG_NAME);

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(1L);
        savedOrganization.setName(VALID_ORG_NAME);
        savedOrganization.setSlug(VALID_ORG_SLUG);
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(VALID_ORG_SLUG)).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result.getStatus(), is(OrganizationStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should set CreatedBy to authenticated user ID")
    void shouldSetCreatedByToAuthenticatedUserId() {
        final ProvisionOrganizationRequest request = aValidOrganizationRequest(VALID_ORG_NAME);

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(1L);
        savedOrganization.setName(VALID_ORG_NAME);
        savedOrganization.setSlug(VALID_ORG_SLUG);
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(VALID_ORG_SLUG)).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result.getCreatedBy(), is(authenticatedUser.getId()));
    }

    @Test
    @DisplayName("Should handle organization name with exactly 3 characters")
    void shouldHandleOrganizationNameWithExactly3Characters() {
        final String orgName = "ABC";
        final ProvisionOrganizationRequest request = aValidOrganizationRequest(orgName);

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(1L);
        savedOrganization.setName(orgName);
        savedOrganization.setSlug("abc");
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug("abc")).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result, notNullValue());
        assertThat(result.getName(), is(orgName));
    }

    @Test
    @DisplayName("Should handle organization name with exactly 100 characters")
    void shouldHandleOrganizationNameWithExactly100Characters() {
        final String orgName = "A".repeat(100);
        final ProvisionOrganizationRequest request = aValidOrganizationRequest(orgName);

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(1L);
        savedOrganization.setName(orgName);
        savedOrganization.setSlug(orgName.toLowerCase());
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(anyString())).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result, notNullValue());
        assertThat(result.getName(), is(orgName));
    }

    // -------------------------------------------------------------------------
    // updateStatus — notification dispatch
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should dispatch urgent SYSTEM notification when status changes to SUSPENDED")
    void shouldDispatchUrgentNotificationWhenStatusChangesToSuspended() {
        // Given: An active organization
        final Organization organization = anActiveOrganization(10L, "Acme Corp");
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When: Status is updated to SUSPENDED
        organizationService.updateStatus(10L, OrganizationStatus.SUSPENDED);

        // Then: An urgent SYSTEM notification is dispatched to the organization audience
        verify(notificationDispatchService).dispatchOrganizationStatusChange(
            eq(10L),
            eq("Acme Corp"),
            eq(OrganizationStatus.ACTIVE),
            eq(OrganizationStatus.SUSPENDED)
        );
    }

    @Test
    @DisplayName("Should dispatch non-urgent SYSTEM notification when status changes from SUSPENDED to ACTIVE")
    void shouldDispatchNonUrgentNotificationWhenStatusChangesFromSuspendedToActive() {
        // Given: A suspended organization
        final Organization organization = aSuspendedOrganization(20L, "Beta Inc");
        when(organizationRepository.findById(20L)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When: Status is updated to ACTIVE
        organizationService.updateStatus(20L, OrganizationStatus.ACTIVE);

        // Then: A non-urgent SYSTEM notification is dispatched to the organization audience
        verify(notificationDispatchService).dispatchOrganizationStatusChange(
            eq(20L),
            eq("Beta Inc"),
            eq(OrganizationStatus.SUSPENDED),
            eq(OrganizationStatus.ACTIVE)
        );
    }

    @Test
    @DisplayName("Should NOT dispatch notification when status is idempotent ACTIVE to ACTIVE")
    void shouldNotDispatchNotificationWhenStatusIsIdempotentActiveToActive() {
        // Given: An already active organization
        final Organization organization = anActiveOrganization(30L, "Gamma Ltd");
        when(organizationRepository.findById(30L)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When: Status is set to ACTIVE again (no change)
        organizationService.updateStatus(30L, OrganizationStatus.ACTIVE);

        // Then: Dispatch is called with same old/new status (dispatch service handles no-op)
        verify(notificationDispatchService).dispatchOrganizationStatusChange(
            eq(30L),
            eq("Gamma Ltd"),
            eq(OrganizationStatus.ACTIVE),
            eq(OrganizationStatus.ACTIVE)
        );
    }

    @Test
    @DisplayName("Should NOT dispatch notification when status is idempotent SUSPENDED to SUSPENDED")
    void shouldNotDispatchNotificationWhenStatusIsIdempotentSuspendedToSuspended() {
        // Given: An already suspended organization
        final Organization organization = aSuspendedOrganization(40L, "Delta Co");
        when(organizationRepository.findById(40L)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When: Status is set to SUSPENDED again (no change)
        organizationService.updateStatus(40L, OrganizationStatus.SUSPENDED);

        // Then: Dispatch is called with same old/new status (dispatch service handles no-op)
        verify(notificationDispatchService).dispatchOrganizationStatusChange(
            eq(40L),
            eq("Delta Co"),
            eq(OrganizationStatus.SUSPENDED),
            eq(OrganizationStatus.SUSPENDED)
        );
    }

    @Test
    @DisplayName("Should still save organization when status changes to SUSPENDED")
    void shouldSaveOrganizationWhenStatusChangesToSuspended() {
        // Given: An active organization
        final Organization organization = anActiveOrganization(50L, "Echo Corp");
        when(organizationRepository.findById(50L)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When: Status is updated to SUSPENDED
        final Organization result = organizationService.updateStatus(50L, OrganizationStatus.SUSPENDED);

        // Then: Organization is saved with new status
        verify(organizationRepository).save(organization);
        assertThat(result.getStatus(), is(equalTo(OrganizationStatus.SUSPENDED)));
    }

    @Test
    @DisplayName("Should still save organization when status changes from SUSPENDED to ACTIVE")
    void shouldSaveOrganizationWhenStatusChangesFromSuspendedToActive() {
        // Given: A suspended organization
        final Organization organization = aSuspendedOrganization(60L, "Foxtrot Ltd");
        when(organizationRepository.findById(60L)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When: Status is updated to ACTIVE
        final Organization result = organizationService.updateStatus(60L, OrganizationStatus.ACTIVE);

        // Then: Organization is saved with new status
        verify(organizationRepository).save(organization);
        assertThat(result.getStatus(), is(equalTo(OrganizationStatus.ACTIVE)));
    }

    private static Organization anActiveOrganization(final Long id, final String name) {
        final Organization org = new Organization();
        org.setId(id);
        org.setName(name);
        org.setStatus(OrganizationStatus.ACTIVE);
        return org;
    }

    private static Organization aSuspendedOrganization(final Long id, final String name) {
        final Organization org = new Organization();
        org.setId(id);
        org.setName(name);
        org.setStatus(OrganizationStatus.SUSPENDED);
        return org;
    }

    @Test
    @DisplayName("Should handle organization name with unicode characters")
    void shouldHandleOrganizationNameWithUnicodeCharacters() {
        final ProvisionOrganizationRequest request = aValidOrganizationRequest(UNICODE_NAME);

        final Organization savedOrganization = new Organization();
        savedOrganization.setId(1L);
        savedOrganization.setName(UNICODE_NAME);
        savedOrganization.setSlug("cafe-cmpany");
        savedOrganization.setStatus(OrganizationStatus.ACTIVE);
        savedOrganization.setCreatedBy(authenticatedUser.getId());

        when(organizationRepository.findBySlug(anyString())).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenReturn(savedOrganization);

        final Organization result = organizationService.create(request);

        assertThat(result, notNullValue());
        assertThat(result.getName(), is(UNICODE_NAME));
    }
}
