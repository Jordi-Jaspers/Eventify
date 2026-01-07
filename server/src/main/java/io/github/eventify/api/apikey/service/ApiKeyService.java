package io.github.eventify.api.apikey.service;

import io.github.eventify.api.apikey.model.*;
import io.github.eventify.api.apikey.model.request.CreateApiKeyRequest;
import io.github.eventify.api.apikey.repository.ApiKeyAuditRepository;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.ApiKeyLimitExceededException;
import io.github.eventify.common.security.SecurityUtil;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.API_KEY_NOT_FOUND;

/**
 * Service for managing user API keys.
 */
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final int USER_KEY_LIMIT = 5;

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyAuditRepository apiKeyAuditRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user API key.
     *
     * @param request the create request
     * @return creation result with API key entity and full key
     */
    @Transactional
    public ApiKey createUserApiKey(final CreateApiKeyRequest request) {
        final User user = SecurityUtil.getLoggedInUser();
        final Long keyCount = apiKeyRepository.countByUserIdAndOrganizationIdIsNull(user.getId());
        if (keyCount >= USER_KEY_LIMIT) {
            throw new ApiKeyLimitExceededException();
        }

        final GeneratedApiKey generatedKey = ApiKeyGenerator.generate(ApiKeyScope.USER);
        final ApiKey apiKey = new ApiKey();
        apiKey.setSuffix(generatedKey.getSuffix());
        apiKey.setHashedKey(passwordEncoder.encode(generatedKey.getFullKey()));
        apiKey.setKey(generatedKey.getFullKey());
        apiKey.setName(request.getName());
        apiKey.setScope(ApiKeyScope.USER);
        apiKey.setUser(user);
        apiKey.setOrganization(null);
        apiKey.setCreatedAt(OffsetDateTime.now());
        apiKey.setExpiresAt(request.getExpiresAt());
        return apiKeyRepository.save(apiKey);
    }

    /**
     * List all user API keys (personal keys only).
     *
     * @return list of API keys
     */
    @Transactional(readOnly = true)
    public List<ApiKey> listUserApiKeys() {
        final User user = SecurityUtil.getLoggedInUser();
        return apiKeyRepository.findAllByUserIdAndOrganizationIdIsNull(user.getId());
    }

    /**
     * Revoke a user API key.
     *
     * @param keyId the API key ID to revoke
     */
    @Transactional
    public void revokeUserApiKey(final Long keyId) {
        final User user = SecurityUtil.getLoggedInUser();
        final ApiKey apiKey = apiKeyRepository.findByIdAndUserId(keyId, user.getId())
            .orElseThrow(() -> new DataNotFoundException(API_KEY_NOT_FOUND));

        final ApiKeyAudit audit = new ApiKeyAudit();
        audit.setKeySuffix(apiKey.getSuffix());
        audit.setKeyName(apiKey.getName());
        audit.setScope(apiKey.getScope());
        audit.setOwnerUserId(user.getId());
        audit.setOrganizationId(null);
        audit.setCreatedBy(user.getId());
        audit.setCreatedAt(apiKey.getCreatedAt());
        audit.setRevokedBy(user);
        audit.setRevokedAt(OffsetDateTime.now());
        audit.setTotalRequests(apiKey.getTotalRequests());
        apiKeyAuditRepository.save(audit);
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
