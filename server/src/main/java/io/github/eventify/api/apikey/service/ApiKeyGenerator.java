package io.github.eventify.api.apikey.service;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.model.GeneratedApiKey;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Utility class for generating and validating API keys.
 * Single source of truth for key format rules.
 */
@UtilityClass
public final class ApiKeyGenerator {

    private static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_LENGTH = 32;
    private static final int SUFFIX_LENGTH = 4;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a new API key with the specified scope.
     *
     * @param scope the scope of the API key
     * @return the generated API key
     */
    public static GeneratedApiKey generate(final ApiKeyScope scope) {
        final String randomPart = generateRandomString(RANDOM_LENGTH);
        final String fullKey = scope.getPrefix() + randomPart;
        return new GeneratedApiKey(fullKey);
    }

    /**
     * Validates the format of an API key.
     * Checks for valid prefix and correct random part length.
     *
     * @param rawKey the raw API key to validate
     * @return true if the key has valid format, false otherwise
     */
    public static boolean isValidFormat(final String rawKey) {
        return rawKey != null && Arrays.stream(ApiKeyScope.values())
            .filter(scope -> rawKey.startsWith(scope.getPrefix()))
            .anyMatch(scope -> rawKey.length() - scope.getPrefix().length() == RANDOM_LENGTH);
    }

    /**
     * Extracts the suffix (last 4 characters) from an API key.
     * Used for database lookup before hash verification.
     *
     * @param rawKey the raw API key
     * @return the suffix
     * @throws IllegalArgumentException if the key is too short
     */
    public static String extractSuffix(final String rawKey) {
        if (rawKey == null || rawKey.length() < SUFFIX_LENGTH) {
            throw new IllegalArgumentException("Key too short to extract suffix");
        }
        return rawKey.substring(rawKey.length() - SUFFIX_LENGTH);
    }

    /**
     * Gets the suffix length used for key generation.
     *
     * @return the suffix length
     */
    public static int getSuffixLength() {
        return SUFFIX_LENGTH;
    }

    /**
     * Generates a random string using lowercase letters and digits only.
     *
     * @param length the length of the random string
     * @return the random string
     */
    private static String generateRandomString(final int length) {
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final int randomIndex = SECURE_RANDOM.nextInt(ALLOWED_CHARS.length());
            sb.append(ALLOWED_CHARS.charAt(randomIndex));
        }
        return sb.toString();
    }
}
