package io.github.eventify.api.apikey.service;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyAudit;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.model.GeneratedApiKey;
import io.github.eventify.api.apikey.model.request.CreateApiKeyRequest;
import io.github.eventify.api.apikey.repository.ApiKeyAuditRepository;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.organization.service.OrganizationService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.ApiKeyLimitExceededException;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - API Key Service")
public class ApiKeyServiceTest extends UnitTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ApiKeyAuditRepository apiKeyAuditRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ApiKeyService apiKeyService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private MockedStatic<ApiKeyGenerator> apiKeyGeneratorMock;
    private User user;

    @BeforeEach
    public void setUp() {
        user = aValidUser();
        user.setId(1L);

        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(user);

        apiKeyGeneratorMock = mockStatic(ApiKeyGenerator.class);
    }

    @AfterEach
    public void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
        if (apiKeyGeneratorMock != null) {
            apiKeyGeneratorMock.close();
        }
    }

    @Test
    @DisplayName("Should create user API key successfully")
    public void shouldCreateUserApiKeySuccessfully() {
        // Given: A valid create request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Production Server");

        final GeneratedApiKey generatedKey = new GeneratedApiKey("evt_abcdefghijklmnopqrstuvwxyz123456", passwordEncoder);
        apiKeyGeneratorMock.when(() -> ApiKeyGenerator.generate(ApiKeyScope.USER)).thenReturn(generatedKey);

        when(passwordEncoder.encode(anyString())).thenReturn("hashed_key");
        when(apiKeyRepository.countByUserIdAndOrganizationIdIsNull(user.getId())).thenReturn(2L);
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> {
            final ApiKey key = invocation.getArgument(0);
            key.setId(1L);
            return key;
        });

        // When: Creating user API key
        final ApiKey apiKey = apiKeyService.createUserApiKey(request);

        // Then: Result should contain API key with full key
        assertThat(apiKey, is(notNullValue()));
        assertThat(apiKey.getId(), is(1L));
        assertThat(apiKey.getName(), is("Production Server"));
        assertThat(apiKey.getKey(), startsWith("evt_"));
        assertThat(apiKey.getSuffix(), is(notNullValue()));
        assertThat(apiKey.getSuffix().length(), is(4));
        assertThat(apiKey.getCreatedAt(), is(notNullValue()));

        verify(apiKeyRepository).save(any(ApiKey.class));
    }

    @Test
    @DisplayName("Should create API key with expiration date")
    public void shouldCreateApiKeyWithExpirationDate() {
        // Given: A valid request with future expiration
        final OffsetDateTime futureExpiration = OffsetDateTime.now().plusDays(30);
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Temporary Key")
            .setExpiresAt(futureExpiration);

        final GeneratedApiKey generatedKey = new GeneratedApiKey("evt_abcdefghijklmnopqrstuvwxyz123456", passwordEncoder);
        apiKeyGeneratorMock.when(() -> ApiKeyGenerator.generate(ApiKeyScope.USER)).thenReturn(generatedKey);

        when(passwordEncoder.encode(anyString())).thenReturn("hashed_key");
        when(apiKeyRepository.countByUserIdAndOrganizationIdIsNull(user.getId())).thenReturn(2L);
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> {
            final ApiKey key = invocation.getArgument(0);
            key.setId(1L);
            return key;
        });

        // When: Creating API key
        final ApiKey apiKey = apiKeyService.createUserApiKey(request);

        // Then: Result should include expiration
        assertThat(apiKey.getExpiresAt(), is(notNullValue()));

        verify(apiKeyRepository).save(any(ApiKey.class));
    }

    @Test
    @DisplayName("Should throw when user has 5 API keys already")
    public void shouldThrowWhenUserHas5ApiKeys() {
        // Given: User already has 5 API keys
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Sixth Key");

        when(apiKeyRepository.countByUserIdAndOrganizationIdIsNull(user.getId())).thenReturn(5L);

        // When & Then: Should throw ApiKeyLimitExceededException
        assertThrows(
            ApiKeyLimitExceededException.class,
            () -> apiKeyService.createUserApiKey(request)
        );

        verify(apiKeyRepository, never()).save(any(ApiKey.class));
    }

    @Test
    @DisplayName("Should generate key with correct prefix and hash")
    public void shouldGenerateKeyWithCorrectPrefixAndHash() {
        // Given: A valid request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Test Key");

        final String fullKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        final GeneratedApiKey generatedKey = new GeneratedApiKey(fullKey, passwordEncoder);

        apiKeyGeneratorMock.when(() -> ApiKeyGenerator.generate(ApiKeyScope.USER)).thenReturn(generatedKey);

        when(passwordEncoder.encode(fullKey)).thenReturn("hashed_key_value");
        when(apiKeyRepository.countByUserIdAndOrganizationIdIsNull(user.getId())).thenReturn(0L);
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> {
            final ApiKey key = invocation.getArgument(0);
            key.setId(1L);
            return key;
        });

        // When: Creating API key
        apiKeyService.createUserApiKey(request);

        // Then: Should save with hashed key
        verify(apiKeyRepository).save(
            argThat(
                apiKey -> apiKey.getHashedKey() != null &&
                    apiKey.getScope().getPrefix().startsWith("evt_") &&
                    apiKey.getScope() == ApiKeyScope.USER
            )
        );
    }

    @Test
    @DisplayName("Should list user API keys successfully")
    public void shouldListUserApiKeysSuccessfully() {
        // Given: User has API keys
        final ApiKey key1 = anApiKey(1L, "efgh", "Production", user);
        final ApiKey key2 = anApiKey(2L, "mnop", "Development", user);

        when(apiKeyRepository.findAllByUserIdAndOrganizationIdIsNull(user.getId()))
            .thenReturn(List.of(key1, key2));

        // When: Listing user API keys
        final List<ApiKey> keys = apiKeyService.listUserApiKeys();

        // Then: Should return list of API keys
        assertThat(keys, is(notNullValue()));
        assertThat(keys, hasSize(2));
        assertThat(keys.get(0).getName(), is("Production"));
        assertThat(keys.get(1).getName(), is("Development"));
    }

    @Test
    @DisplayName("Should build correct masked display format")
    public void shouldBuildCorrectMaskedDisplayFormat() {
        // Given: User has API key with known suffix
        final ApiKey key = anApiKey(1L, "efgh", "Test Key", user);

        when(apiKeyRepository.findAllByUserIdAndOrganizationIdIsNull(user.getId()))
            .thenReturn(List.of(key));

        // When: Listing API keys
        final List<ApiKey> keys = apiKeyService.listUserApiKeys();

        // Then: Masked key should be evt_******<suffix>
        final String maskedKey = keys.get(0).getMaskedKey();
        assertThat(maskedKey, startsWith("evt_******"));
        assertThat(maskedKey.substring(10), is("efgh"));
    }

    @Test
    @DisplayName("Should return only personal keys excluding organization keys")
    public void shouldReturnOnlyPersonalKeys() {
        // Given: User has personal keys
        final ApiKey personalKey = anApiKey(1L, "onal", "Personal", user);
        personalKey.setOrganization(null);

        when(apiKeyRepository.findAllByUserIdAndOrganizationIdIsNull(user.getId()))
            .thenReturn(List.of(personalKey));

        // When: Listing user API keys
        final List<ApiKey> keys = apiKeyService.listUserApiKeys();

        // Then: Should return only personal keys
        assertThat(keys, hasSize(1));
        assertThat(keys.get(0).getName(), is("Personal"));

        verify(apiKeyRepository).findAllByUserIdAndOrganizationIdIsNull(user.getId());
    }

    @Test
    @DisplayName("Should return empty list when user has no keys")
    public void shouldReturnEmptyListWhenUserHasNoKeys() {
        // Given: User has no API keys
        when(apiKeyRepository.findAllByUserIdAndOrganizationIdIsNull(user.getId()))
            .thenReturn(List.of());

        // When: Listing user API keys
        final List<ApiKey> keys = apiKeyService.listUserApiKeys();

        // Then: Should return empty list
        assertThat(keys, is(empty()));
    }

    @Test
    @DisplayName("Should revoke user API key successfully")
    public void shouldRevokeUserApiKeySuccessfully() {
        // Given: User has an API key
        final ApiKey key = anApiKey(1L, "efgh", "Key to Revoke", user);

        when(apiKeyRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(key));

        // When: Revoking the API key
        apiKeyService.revokeApiKey(1L, user, null);

        // Then: Key should be deleted
        verify(apiKeyRepository).delete(key);
    }

    @Test
    @DisplayName("Should create audit record when revoking API key")
    public void shouldCreateAuditRecordWhenRevokingApiKey() {
        // Given: User has an API key
        final ApiKey key = anApiKey(1L, "efgh", "Audited Key", user);
        key.setTotalRequests(100L);

        when(apiKeyRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(key));

        // When: Revoking the API key
        apiKeyService.revokeApiKey(1L, user, null);

        // Then: Audit record should be created
        verify(apiKeyAuditRepository).save(
            argThat(
                audit -> audit.getKeySuffix().equals("efgh") &&
                    audit.getKeyName().equals("Audited Key") &&
                    audit.getScope() == ApiKeyScope.USER &&
                    audit.getOwnerUserId().equals(user.getId()) &&
                    audit.getRevokedBy().getId().equals(user.getId()) &&
                    audit.getTotalRequests().equals(100L)
            )
        );
    }

    @Test
    @DisplayName("Should throw when API key not found")
    public void shouldThrowWhenApiKeyNotFound() {
        // Given: API key does not exist
        when(apiKeyRepository.findByIdAndUserId(999L, user.getId())).thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> apiKeyService.revokeApiKey(999L, user, null)
        );

        verify(apiKeyRepository, never()).delete(any(ApiKey.class));
        verify(apiKeyAuditRepository, never()).save(any(ApiKeyAudit.class));
    }

    @Test
    @DisplayName("Should throw when key belongs to another user")
    public void shouldThrowWhenKeyBelongsToAnotherUser() {
        // Given: API key does not belong to current user
        when(apiKeyRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> apiKeyService.revokeApiKey(1L, user, null)
        );

        verify(apiKeyRepository, never()).delete(any(ApiKey.class));
    }

    @Test
    @DisplayName("Should count only personal keys excluding organization keys")
    public void shouldCountOnlyPersonalKeys() {
        // Given: User has 3 personal keys
        when(apiKeyRepository.countByUserIdAndOrganizationIdIsNull(user.getId())).thenReturn(3L);

        // When: Creating new key (validates count)
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Fourth Key");

        final GeneratedApiKey generatedKey = new GeneratedApiKey("evt_newkey123456789012345678901234", passwordEncoder);
        apiKeyGeneratorMock.when(() -> ApiKeyGenerator.generate(ApiKeyScope.USER)).thenReturn(generatedKey);

        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> {
            final ApiKey key = invocation.getArgument(0);
            key.setId(4L);
            return key;
        });

        apiKeyService.createUserApiKey(request);

        // Then: Should verify count excludes organization keys
        verify(apiKeyRepository).countByUserIdAndOrganizationIdIsNull(user.getId());
    }

    @Test
    @DisplayName("Should include usage statistics in list response")
    public void shouldIncludeUsageStatisticsInListResponse() {
        // Given: User has API key with usage stats
        final ApiKey key = anApiKey(1L, "efgh", "Used Key", user);
        key.setTotalRequests(1542L);
        key.setLastUsedAt(OffsetDateTime.now().minusHours(2));

        when(apiKeyRepository.findAllByUserIdAndOrganizationIdIsNull(user.getId()))
            .thenReturn(List.of(key));

        // When: Listing API keys
        final List<ApiKey> keys = apiKeyService.listUserApiKeys();

        // Then: Keys should include usage stats
        final ApiKey keyResult = keys.get(0);
        assertThat(keyResult.getTotalRequests(), is(1542L));
        assertThat(keyResult.getLastUsedAt(), is(notNullValue()));
    }

}
