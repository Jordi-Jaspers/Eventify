package io.github.eventify.api.apikey.service;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.common.exception.ApiKeyExpiredException;
import io.github.eventify.common.exception.InvalidApiKeyException;
import io.github.eventify.common.exception.UserDisabledException;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.*;

/**
 * Service for authenticating API keys.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyAuthenticationService {

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate an API key.
     *
     * @param rawKey the raw API key
     * @return the API key principal
     * @throws InvalidApiKeyException if the key is invalid
     * @throws ApiKeyExpiredException if the key has expired
     * @throws UserDisabledException  if the user is disabled
     */
    @Transactional
    public ApiKeyPrincipal authenticate(final String rawKey) {
        if (!ApiKeyGenerator.isValidFormat(rawKey)) {
            throw new InvalidApiKeyException(INVALID_API_KEY);
        }

        final String suffix = ApiKeyGenerator.extractSuffix(rawKey);
        final ApiKey apiKey = apiKeyRepository.findBySuffix(suffix)
            .orElseThrow(() -> new InvalidApiKeyException(INVALID_API_KEY));

        if (!passwordEncoder.matches(rawKey, apiKey.getHashedKey())) {
            log.debug("API key hash mismatch for suffix: {}", suffix);
            throw new InvalidApiKeyException(INVALID_API_KEY);
        }

        if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(OffsetDateTime.now())) {
            log.debug("API key expired for suffix: {}", suffix);
            throw new ApiKeyExpiredException(API_KEY_EXPIRED);
        }

        if (apiKey.getScope() == ApiKeyScope.USER && !apiKey.getUser().isEnabled()) {
            log.debug("User disabled for API key suffix: {}", suffix);
            throw new UserDisabledException(API_KEY_USER_DISABLED);
        }

        updateUsageStats(apiKey);
        return new ApiKeyPrincipal(apiKey);
    }

    private void updateUsageStats(final ApiKey apiKey) {
        apiKey.setLastUsedAt(OffsetDateTime.now());
        apiKey.setTotalRequests(apiKey.getTotalRequests() + 1);
        apiKeyRepository.save(apiKey);
    }
}
