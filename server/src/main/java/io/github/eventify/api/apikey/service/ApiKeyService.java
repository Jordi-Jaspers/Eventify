package io.github.eventify.api.apikey.service;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.model.GeneratedApiKey;
import io.github.eventify.api.apikey.model.request.CreateApiKeyRequest;
import io.github.eventify.api.apikey.repository.ApiKeyAuditRepository;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.service.OrganizationService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.ApiKeyLimitExceededException;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.API_KEY_NOT_FOUND;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static java.util.Objects.isNull;

/**
 * Service for managing API keys (user and organization).
 */
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final int USER_KEY_LIMIT = 5;

    private final ApiKeyRepository apiKeyRepository;

    private final ApiKeyAuditRepository apiKeyAuditRepository;

    private final OrganizationService organizationService;

    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user API key.
     *
     * @param request the create request
     * @return creation result with API key entity and full key
     */
    @Transactional
    public ApiKey createUserApiKey(final CreateApiKeyRequest request) {
        final User user = getLoggedInUser();
        final Long keyCount = apiKeyRepository.countByUserIdAndOrganizationIdIsNull(user.getId());
        if (keyCount >= USER_KEY_LIMIT) {
            throw new ApiKeyLimitExceededException();
        }

        final GeneratedApiKey generated = ApiKeyGenerator.generate(ApiKeyScope.USER);
        final String hashedKey = passwordEncoder.encode(generated.getFullKey());
        final ApiKey apiKey = new ApiKey(
            request.getName(),
            request.getExpiresAt(),
            user,
            null,
            ApiKeyScope.USER,
            hashedKey,
            generated
        );
        return apiKeyRepository.save(apiKey);
    }

    /**
     * Create a new organization API key.
     *
     * @param orgId   the organization ID
     * @param creator the user creating the key
     * @param request the create request
     * @return creation result with API key entity and full key
     */
    @Transactional
    public ApiKey createOrganizationApiKey(final Long orgId, final User creator, final CreateApiKeyRequest request) {
        final Organization organization = organizationService.findOrganizationById(orgId);
        final GeneratedApiKey generated = ApiKeyGenerator.generate(ApiKeyScope.ORGANIZATION);
        final String hashedKey = passwordEncoder.encode(generated.getFullKey());
        final ApiKey apiKey = new ApiKey(
            request.getName(),
            request.getExpiresAt(),
            creator,
            organization,
            ApiKeyScope.ORGANIZATION,
            hashedKey,
            generated
        );
        return apiKeyRepository.save(apiKey);
    }

    /**
     * List all user API keys (personal keys only).
     *
     * @return list of API keys
     */
    @Transactional(readOnly = true)
    public List<ApiKey> listUserApiKeys() {
        return apiKeyRepository.findAllByUserIdAndOrganizationIdIsNull(getLoggedInUser().getId());
    }

    /**
     * List all organization API keys.
     *
     * @param orgId the organization ID
     * @return list of organization API keys
     */
    @Transactional(readOnly = true)
    public List<ApiKey> listOrganizationApiKeys(final Long orgId) {
        organizationService.findOrganizationById(orgId);
        return apiKeyRepository.findByOrganizationIdOrderByCreatedAtDesc(orgId);
    }

    /**
     * Revoke an API key (user or organization).
     *
     * @param keyId   the API key ID to revoke
     * @param revoker the user revoking the key
     * @param orgId   the organization ID (null for user keys)
     */
    @Transactional
    public void revokeApiKey(final Long keyId, final User revoker, final Long orgId) {
        final ApiKey apiKey = isNull(orgId)
            ? apiKeyRepository.findByIdAndUserId(keyId, revoker.getId()).orElse(null)
            : apiKeyRepository.findByIdAndOrganizationId(keyId, orgId).orElse(null);

        if (isNull(apiKey)) {
            throw new DataNotFoundException(API_KEY_NOT_FOUND);
        }
        apiKeyAuditRepository.save(apiKey.toAuditRecord(revoker));
        apiKeyRepository.delete(apiKey);
    }

    /**
     * Get the maximum number of user API keys allowed.
     *
     * @return the user API key limit
     */
    public int getUserKeyLimit() {
        return USER_KEY_LIMIT;
    }
}
