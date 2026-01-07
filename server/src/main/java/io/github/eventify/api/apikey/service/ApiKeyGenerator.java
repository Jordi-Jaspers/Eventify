package io.github.eventify.api.apikey.service;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.model.GeneratedApiKey;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

/**
 * Utility class for generating API keys.
 */
@UtilityClass
public final class ApiKeyGenerator {

    private static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_LENGTH = 32;
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
