package io.github.eventify.api.admin.service;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyAudit;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.model.GeneratedApiKey;
import io.github.eventify.api.apikey.repository.ApiKeyAuditRepository;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Admin API Key Service")
public class AdminApiKeyServiceTest extends UnitTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ApiKeyAuditRepository apiKeyAuditRepository;

    @InjectMocks
    private AdminApiKeyService adminApiKeyService;

    private User adminUser;
    private User regularUser;
    private ApiKey userApiKey;
    private ApiKey orgApiKey;
    private Organization organization;

    @BeforeEach
    public void setUp() {
        // Given: An admin user
        adminUser = aValidUser();
        adminUser.setRole(Role.ADMIN);

        // And: A regular user
        regularUser = aValidUser();
        regularUser.setId(2L);
        regularUser.setEmail("regular@example.com");

        // And: An organization
        organization = new Organization();
        organization.setId(1L);
        organization.setName("Test Org");
        organization.setSlug("test-org");

        // And: A user API key
        final GeneratedApiKey userGenerated = new GeneratedApiKey("evt_abcdefghij1234567890");
        userApiKey = new ApiKey(
            "User Test Key",
            OffsetDateTime.now().plusDays(30),
            regularUser,
            null,
            ApiKeyScope.USER,
            "hashed-key",
            userGenerated
        );
        userApiKey.setId(1L);
        userApiKey.setCreatedAt(OffsetDateTime.now().minusDays(5));
        userApiKey.setTotalRequests(100L);

        // And: An organization API key
        final GeneratedApiKey orgGenerated = new GeneratedApiKey("org_abcdefghij1234567890");
        orgApiKey = new ApiKey(
            "Org Test Key",
            null,
            regularUser,
            organization,
            ApiKeyScope.ORGANIZATION,
            "hashed-key",
            orgGenerated
        );
        orgApiKey.setId(2L);
        orgApiKey.setCreatedAt(OffsetDateTime.now().minusDays(10));
        orgApiKey.setTotalRequests(250L);
    }

    @Test
    @DisplayName("Should revoke user API key successfully")
    public void shouldRevokeUserApiKeySuccessfully() {
        // Given: User API key exists
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the key
        adminApiKeyService.revokeApiKey(1L, adminUser);

        // Then: Key should be deleted
        verify(apiKeyRepository).delete(userApiKey);

        // And: Audit record should be created
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit, is(notNullValue()));
        assertThat(audit.getKeySuffix(), is(equalTo("7890")));
        assertThat(audit.getKeyName(), is(equalTo("User Test Key")));
        assertThat(audit.getScope(), is(equalTo(ApiKeyScope.USER)));
        assertThat(audit.getRevokedBy(), is(equalTo(adminUser)));
        assertThat(audit.getTotalRequests(), is(equalTo(100L)));
    }

    @Test
    @DisplayName("Should revoke organization API key successfully")
    public void shouldRevokeOrganizationApiKeySuccessfully() {
        // Given: Organization API key exists
        when(apiKeyRepository.findById(2L)).thenReturn(Optional.of(orgApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the key
        adminApiKeyService.revokeApiKey(2L, adminUser);

        // Then: Key should be deleted
        verify(apiKeyRepository).delete(orgApiKey);

        // And: Audit record should be created with org info
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit, is(notNullValue()));
        assertThat(audit.getKeySuffix(), is(equalTo("7890")));
        assertThat(audit.getKeyName(), is(equalTo("Org Test Key")));
        assertThat(audit.getScope(), is(equalTo(ApiKeyScope.ORGANIZATION)));
        assertThat(audit.getOrganizationId(), is(equalTo(1L)));
        assertThat(audit.getRevokedBy(), is(equalTo(adminUser)));
        assertThat(audit.getTotalRequests(), is(equalTo(250L)));
    }

    @Test
    @DisplayName("Should throw exception when API key not found")
    public void shouldThrowExceptionWhenApiKeyNotFound() {
        // Given: API key does not exist
        when(apiKeyRepository.findById(99L)).thenReturn(Optional.empty());

        // When: Attempting to revoke non-existent key
        // Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> adminApiKeyService.revokeApiKey(99L, adminUser)
        );

        // And: No deletion or audit should occur
        verify(apiKeyRepository, never()).delete(any(ApiKey.class));
        verify(apiKeyAuditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle key with null expiration date")
    public void shouldHandleKeyWithNullExpirationDate() {
        // Given: Key with no expiration
        userApiKey.setExpiresAt(null);
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the key
        adminApiKeyService.revokeApiKey(1L, adminUser);

        // Then: Key should be revoked successfully
        verify(apiKeyRepository).delete(userApiKey);
        verify(apiKeyAuditRepository).save(any(ApiKeyAudit.class));
    }

    @Test
    @DisplayName("Should handle key with null last used date")
    public void shouldHandleKeyWithNullLastUsedDate() {
        // Given: Key that was never used
        userApiKey.setLastUsedAt(null);
        userApiKey.setTotalRequests(0L);
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the key
        adminApiKeyService.revokeApiKey(1L, adminUser);

        // Then: Key should be revoked successfully
        verify(apiKeyRepository).delete(userApiKey);

        // And: Audit should record zero requests
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit.getTotalRequests(), is(equalTo(0L)));
    }

    @Test
    @DisplayName("Should handle expired API key")
    public void shouldHandleExpiredApiKey() {
        // Given: Expired API key
        userApiKey.setExpiresAt(OffsetDateTime.now().minusDays(5));
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the expired key
        adminApiKeyService.revokeApiKey(1L, adminUser);

        // Then: Key should be revoked successfully
        verify(apiKeyRepository).delete(userApiKey);
        verify(apiKeyAuditRepository).save(any(ApiKeyAudit.class));
    }

    @Test
    @DisplayName("Should create audit record with correct timestamps")
    public void shouldCreateAuditRecordWithCorrectTimestamps() {
        // Given: API key exists
        final OffsetDateTime keyCreatedAt = OffsetDateTime.now().minusDays(10);
        userApiKey.setCreatedAt(keyCreatedAt);
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the key
        final OffsetDateTime beforeRevoke = OffsetDateTime.now().minusSeconds(1);
        adminApiKeyService.revokeApiKey(1L, adminUser);
        final OffsetDateTime afterRevoke = OffsetDateTime.now().plusSeconds(1);

        // Then: Audit record should have correct timestamps
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit.getCreatedAt(), is(equalTo(keyCreatedAt)));
        assertThat(audit.getRevokedAt(), is(greaterThanOrEqualTo(beforeRevoke)));
        assertThat(audit.getRevokedAt(), is(lessThanOrEqualTo(afterRevoke)));
    }

    @Test
    @DisplayName("Should create audit record with correct owner information for user key")
    public void shouldCreateAuditRecordWithCorrectOwnerInfoForUserKey() {
        // Given: User API key
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the key
        adminApiKeyService.revokeApiKey(1L, adminUser);

        // Then: Audit should contain user owner info
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit.getOwnerUserId(), is(equalTo(regularUser.getId())));
        assertThat(audit.getCreatedBy(), is(equalTo(regularUser.getId())));
        assertThat(audit.getOrganizationId(), is(nullValue()));
    }

    @Test
    @DisplayName("Should create audit record with correct owner information for org key")
    public void shouldCreateAuditRecordWithCorrectOwnerInfoForOrgKey() {
        // Given: Organization API key
        when(apiKeyRepository.findById(2L)).thenReturn(Optional.of(orgApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the key
        adminApiKeyService.revokeApiKey(2L, adminUser);

        // Then: Audit should contain org info
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit.getOwnerUserId(), is(equalTo(regularUser.getId())));
        assertThat(audit.getCreatedBy(), is(equalTo(regularUser.getId())));
        assertThat(audit.getOrganizationId(), is(equalTo(organization.getId())));
    }

    @Test
    @DisplayName("Should preserve key prefix in audit record based on scope")
    public void shouldPreserveKeyPrefixInAuditBasedOnScope() {
        // Given: User key with evt_ prefix
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking user key
        adminApiKeyService.revokeApiKey(1L, adminUser);

        // Then: Audit should store suffix correctly
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit.getKeySuffix(), is(equalTo("7890")));
        assertThat(audit.getScope(), is(equalTo(ApiKeyScope.USER)));
    }

    @Test
    @DisplayName("Should handle revocation by different admin user")
    public void shouldHandleRevocationByDifferentAdmin() {
        // Given: Different admin user
        final User otherAdmin = aValidUser();
        otherAdmin.setId(3L);
        otherAdmin.setEmail("other-admin@example.com");
        otherAdmin.setRole(Role.ADMIN);

        // And: API key exists
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Other admin revokes the key
        adminApiKeyService.revokeApiKey(1L, otherAdmin);

        // Then: Audit should record the other admin as revoker
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit.getRevokedBy(), is(equalTo(otherAdmin)));
        assertThat(audit.getOwnerUserId(), is(equalTo(regularUser.getId())));
    }

    @Test
    @DisplayName("Should handle key with high request count")
    public void shouldHandleKeyWithHighRequestCount() {
        // Given: Key with many requests
        userApiKey.setTotalRequests(1_000_000L);
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the key
        adminApiKeyService.revokeApiKey(1L, adminUser);

        // Then: Audit should preserve the request count
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit.getTotalRequests(), is(equalTo(1_000_000L)));
    }

    @Test
    @DisplayName("Should handle revocation of recently created key")
    public void shouldHandleRevocationOfRecentlyCreatedKey() {
        // Given: Recently created key
        userApiKey.setCreatedAt(OffsetDateTime.now().minusMinutes(5));
        userApiKey.setTotalRequests(0L);
        userApiKey.setLastUsedAt(null);
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the new key
        adminApiKeyService.revokeApiKey(1L, adminUser);

        // Then: Key should be revoked successfully
        verify(apiKeyRepository).delete(userApiKey);

        // And: Audit should capture the short lifespan
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit.getTotalRequests(), is(equalTo(0L)));
    }

    @Test
    @DisplayName("Should ensure audit record is saved before key deletion")
    public void shouldEnsureAuditRecordIsSavedBeforeDeletion() {
        // Given: API key exists
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the key
        adminApiKeyService.revokeApiKey(1L, adminUser);

        // Then: Audit should be saved before deletion
        final var inOrder = inOrder(apiKeyAuditRepository, apiKeyRepository);
        inOrder.verify(apiKeyAuditRepository).save(any(ApiKeyAudit.class));
        inOrder.verify(apiKeyRepository).delete(userApiKey);
    }

    @Test
    @DisplayName("Should handle key with special characters in name")
    public void shouldHandleKeyWithSpecialCharactersInName() {
        // Given: Key with special chars in name
        userApiKey.setName("Test Key: Production (v2.0) - [$ENV]");
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(userApiKey));
        when(apiKeyAuditRepository.save(any(ApiKeyAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Revoking the key
        adminApiKeyService.revokeApiKey(1L, adminUser);

        // Then: Audit should preserve the name correctly
        final ArgumentCaptor<ApiKeyAudit> auditCaptor = ArgumentCaptor.forClass(ApiKeyAudit.class);
        verify(apiKeyAuditRepository).save(auditCaptor.capture());

        final ApiKeyAudit audit = auditCaptor.getValue();
        assertThat(audit.getKeyName(), is(equalTo("Test Key: Production (v2.0) - [$ENV]")));
    }
}
