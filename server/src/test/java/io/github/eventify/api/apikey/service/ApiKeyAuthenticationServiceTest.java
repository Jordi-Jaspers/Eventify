package io.github.eventify.api.apikey.service;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.ApiKeyExpiredException;
import io.github.eventify.common.exception.InvalidApiKeyException;
import io.github.eventify.common.exception.UserDisabledException;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - API Key Authentication Service")
public class ApiKeyAuthenticationServiceTest extends UnitTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ApiKeyAuthenticationService apiKeyAuthenticationService;

    private User user;
    private Organization organization;

    @BeforeEach
    public void setUp() {
        user = aValidUser();
        user.setId(1L);

        organization = new Organization();
        organization.setId(100L);
        organization.setName("Test Organization");
    }

    @Test
    @DisplayName("Should authenticate with valid user API key")
    public void shouldAuthenticateWithValidUserApiKey() {
        // Given: A valid user API key
        final String rawKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        final ApiKey apiKey = createUserApiKey(1L, "3456", "Production Key");
        apiKey.setHashedKey("hashed_key");
        apiKey.setExpiresAt(null); // No expiration

        when(apiKeyRepository.findBySuffix("3456")).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(rawKey, "hashed_key")).thenReturn(true);
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

        // When: Authenticating with the API key
        final ApiKeyPrincipal principal = apiKeyAuthenticationService.authenticate(rawKey);

        // Then: Authentication should succeed
        assertThat(principal, is(notNullValue()));
        assertThat(principal.getUser(), is(notNullValue()));
        assertThat(principal.getUser().getId(), is(1L));
        assertThat(principal.getScope(), is(ApiKeyScope.USER));
        assertThat(principal.getOrganizationId(), is(nullValue()));

        // And: Usage stats should be updated
        verify(apiKeyRepository).save(any(ApiKey.class));
    }

    @Test
    @DisplayName("Should authenticate with valid organization API key")
    public void shouldAuthenticateWithValidOrgApiKey() {
        // Given: A valid organization API key
        final String rawKey = "org_abcdefghijklmnopqrstuvwxyz123456";
        final ApiKey apiKey = createOrgApiKey(2L, "3456", "Org Production Key", organization);
        apiKey.setHashedKey("hashed_key");
        apiKey.setExpiresAt(null);

        when(apiKeyRepository.findBySuffix("3456")).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(rawKey, "hashed_key")).thenReturn(true);
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

        // When: Authenticating with organization API key
        final ApiKeyPrincipal principal = apiKeyAuthenticationService.authenticate(rawKey);

        // Then: Authentication should succeed with organization context
        assertThat(principal, is(notNullValue()));
        assertThat(principal.getUser(), is(notNullValue()));
        assertThat(principal.getScope(), is(ApiKeyScope.ORGANIZATION));
        assertThat(principal.getOrganizationId(), is(100L));

        // And: Usage stats should be updated
        verify(apiKeyRepository).save(any(ApiKey.class));
    }

    @Test
    @DisplayName("Should reject expired API key")
    public void shouldRejectExpiredApiKey() {
        // Given: An expired API key
        final String rawKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        final ApiKey apiKey = createUserApiKey(1L, "3456", "Expired Key");
        apiKey.setHashedKey("hashed_key");
        apiKey.setExpiresAt(OffsetDateTime.now().minusDays(1)); // Expired yesterday

        when(apiKeyRepository.findBySuffix("3456")).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(rawKey, "hashed_key")).thenReturn(true);

        // When & Then: Should throw ApiKeyExpiredException
        assertThrows(
            ApiKeyExpiredException.class,
            () -> apiKeyAuthenticationService.authenticate(rawKey)
        );
    }

    @Test
    @DisplayName("Should reject invalid key hash")
    public void shouldRejectInvalidKeyHash() {
        // Given: API key with wrong hash
        final String rawKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        final ApiKey apiKey = createUserApiKey(1L, "3456", "Production Key");
        apiKey.setHashedKey("different_hash");

        when(apiKeyRepository.findBySuffix("3456")).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(rawKey, "different_hash")).thenReturn(false);

        // When & Then: Should throw InvalidApiKeyException
        assertThrows(
            InvalidApiKeyException.class,
            () -> apiKeyAuthenticationService.authenticate(rawKey)
        );
    }

    @Test
    @DisplayName("Should reject malformed API key with wrong prefix")
    public void shouldRejectMalformedKeyWrongPrefix() {
        // Given: Malformed key with wrong prefix
        final String rawKey = "xxx_abcdefghijklmnopqrstuvwxyz123456";

        // When & Then: Should throw InvalidApiKeyException
        assertThrows(
            InvalidApiKeyException.class,
            () -> apiKeyAuthenticationService.authenticate(rawKey)
        );
    }

    @Test
    @DisplayName("Should reject malformed API key too short")
    public void shouldRejectMalformedKeyTooShort() {
        // Given: Malformed key that is too short
        final String rawKey = "evt_abc";

        // When & Then: Should throw InvalidApiKeyException
        assertThrows(
            InvalidApiKeyException.class,
            () -> apiKeyAuthenticationService.authenticate(rawKey)
        );
    }

    @Test
    @DisplayName("Should reject API key when suffix not found")
    public void shouldRejectKeyWhenSuffixNotFound() {
        // Given: API key suffix does not exist in database
        final String rawKey = "evt_abcdefghijklmnopqrstuvwxyz123456";

        when(apiKeyRepository.findBySuffix("3456")).thenReturn(Optional.empty());

        // When & Then: Should throw InvalidApiKeyException
        assertThrows(
            InvalidApiKeyException.class,
            () -> apiKeyAuthenticationService.authenticate(rawKey)
        );
    }

    @Test
    @DisplayName("Should reject API key when user is disabled")
    public void shouldRejectKeyWhenUserDisabled() {
        // Given: API key with disabled user
        final String rawKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        final ApiKey apiKey = createUserApiKey(1L, "3456", "Production Key");
        apiKey.setHashedKey("hashed_key");
        apiKey.getUser().setEnabled(false); // User is disabled

        when(apiKeyRepository.findBySuffix("3456")).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(rawKey, "hashed_key")).thenReturn(true);

        // When & Then: Should throw UserDisabledException
        assertThrows(
            UserDisabledException.class,
            () -> apiKeyAuthenticationService.authenticate(rawKey)
        );
    }

    @Test
    @DisplayName("Should update last used at and total requests")
    public void shouldUpdateLastUsedAtAndTotalRequests() {
        // Given: A valid API key with existing usage stats
        final String rawKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        final ApiKey apiKey = createUserApiKey(1L, "3456", "Production Key");
        apiKey.setHashedKey("hashed_key");
        apiKey.setTotalRequests(100L);
        apiKey.setLastUsedAt(OffsetDateTime.now().minusHours(5));

        when(apiKeyRepository.findBySuffix("3456")).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(rawKey, "hashed_key")).thenReturn(true);
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

        // When: Authenticating with the API key
        apiKeyAuthenticationService.authenticate(rawKey);

        // Then: Should update usage stats
        verify(apiKeyRepository).save(
            org.mockito.ArgumentMatchers.argThat(
                savedKey -> savedKey.getTotalRequests().equals(101L) &&
                    savedKey.getLastUsedAt() != null
            )
        );
    }

    @Test
    @DisplayName("Should grant SEND_EVENTS authority only")
    public void shouldGrantSendEventsAuthorityOnly() {
        // Given: A valid API key
        final String rawKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        final ApiKey apiKey = createUserApiKey(1L, "3456", "Production Key");
        apiKey.setHashedKey("hashed_key");

        when(apiKeyRepository.findBySuffix("3456")).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(rawKey, "hashed_key")).thenReturn(true);
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

        // When: Authenticating with the API key
        final ApiKeyPrincipal principal = apiKeyAuthenticationService.authenticate(rawKey);

        // Then: Principal should have SEND_EVENTS authority
        assertThat(principal.getAuthorities(), is(notNullValue()));
        assertThat(principal.getAuthorities(), hasSize(1));
        assertThat(
            principal.getAuthorities().iterator().next().getAuthority(),
            is("SEND_EVENTS")
        );
    }

    @Test
    @DisplayName("Should handle key with no expiration date")
    public void shouldHandleKeyWithNoExpirationDate() {
        // Given: A valid API key with no expiration
        final String rawKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        final ApiKey apiKey = createUserApiKey(1L, "3456", "Never Expires");
        apiKey.setHashedKey("hashed_key");
        apiKey.setExpiresAt(null);

        when(apiKeyRepository.findBySuffix("3456")).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(rawKey, "hashed_key")).thenReturn(true);
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

        // When: Authenticating with the API key
        final ApiKeyPrincipal principal = apiKeyAuthenticationService.authenticate(rawKey);

        // Then: Authentication should succeed
        assertThat(principal, is(notNullValue()));
        assertThat(principal.getUser(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should handle key with future expiration date")
    public void shouldHandleKeyWithFutureExpirationDate() {
        // Given: A valid API key expiring in the future
        final String rawKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        final ApiKey apiKey = createUserApiKey(1L, "3456", "Expires Later");
        apiKey.setHashedKey("hashed_key");
        apiKey.setExpiresAt(OffsetDateTime.now().plusDays(30));

        when(apiKeyRepository.findBySuffix("3456")).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(rawKey, "hashed_key")).thenReturn(true);
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

        // When: Authenticating with the API key
        final ApiKeyPrincipal principal = apiKeyAuthenticationService.authenticate(rawKey);

        // Then: Authentication should succeed
        assertThat(principal, is(notNullValue()));
        assertThat(principal.getUser(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should extract correct suffix from key")
    public void shouldExtractCorrectSuffixFromKey() {
        // Given: A valid API key with known suffix (4 prefix + 32 random = 36 chars)
        final String rawKey = "evt_abcdefghijklmnopqrstuvwxyz12test";
        final ApiKey apiKey = createUserApiKey(1L, "test", "Test Key");
        apiKey.setHashedKey("hashed_key");

        when(apiKeyRepository.findBySuffix("test")).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(rawKey, "hashed_key")).thenReturn(true);
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

        // When: Authenticating with the API key
        final ApiKeyPrincipal principal = apiKeyAuthenticationService.authenticate(rawKey);

        // Then: Should find key by correct suffix
        assertThat(principal, is(notNullValue()));
        verify(apiKeyRepository).findBySuffix("test");
    }

    private ApiKey createUserApiKey(final Long id, final String suffix, final String name) {
        final ApiKey key = new ApiKey();
        key.setId(id);
        key.setSuffix(suffix);
        key.setName(name);
        key.setScope(ApiKeyScope.USER);
        key.setUser(user);
        key.setOrganization(null);
        key.setCreatedAt(OffsetDateTime.now().minusDays(1));
        key.setTotalRequests(0L);
        return key;
    }

    private ApiKey createOrgApiKey(final Long id, final String suffix, final String name, final Organization org) {
        final ApiKey key = new ApiKey();
        key.setId(id);
        key.setSuffix(suffix);
        key.setName(name);
        key.setScope(ApiKeyScope.ORGANIZATION);
        key.setUser(user);
        key.setOrganization(org);
        key.setCreatedAt(OffsetDateTime.now().minusDays(1));
        key.setTotalRequests(0L);
        return key;
    }
}
