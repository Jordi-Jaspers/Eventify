package io.github.eventify.api.apikey.service;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.model.GeneratedApiKey;
import io.github.eventify.support.UnitTest;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit Test - API Key Generator.
 */
@DisplayName("Unit Test - API Key Generator")
public class ApiKeyGeneratorTest extends UnitTest {

    private static final int KEY_TOTAL_LENGTH = 36; // prefix(3) + underscore(1) + random(32)
    private static final int DISPLAY_PREFIX_LENGTH = 12;
    private static final String USER_PREFIX = "evt";
    private static final String ORG_PREFIX = "org";

    @Test
    @DisplayName("Should generate key with evt prefix for user scope")
    public void shouldGenerateKeyWithEvtPrefixForUserScope() {
        // Given: User scope
        final ApiKeyScope scope = ApiKeyScope.USER;

        // When: Generating API key
        final GeneratedApiKey generatedKey = ApiKeyGenerator.generate(scope);

        // Then: Full key should start with evt_
        assertThat(generatedKey.getFullKey(), startsWith(USER_PREFIX + "_"));
    }

    @Test
    @DisplayName("Should generate key with org prefix for organization scope")
    public void shouldGenerateKeyWithOrgPrefixForOrganizationScope() {
        // Given: Organization scope
        final ApiKeyScope scope = ApiKeyScope.ORGANIZATION;

        // When: Generating API key
        final GeneratedApiKey generatedKey = ApiKeyGenerator.generate(scope);

        // Then: Full key should start with org_
        assertThat(generatedKey.getFullKey(), startsWith(ORG_PREFIX + "_"));
    }

    @Test
    @DisplayName("Should generate key with 36 characters total length")
    public void shouldGenerateKeyWith36CharactersTotalLength() {
        // Given: User scope
        final ApiKeyScope scope = ApiKeyScope.USER;

        // When: Generating API key
        final GeneratedApiKey generatedKey = ApiKeyGenerator.generate(scope);

        // Then: Full key should be 36 characters (prefix + _ + 32 random)
        assertThat(generatedKey.getFullKey(), hasLength(KEY_TOTAL_LENGTH));
    }

    @Test
    @DisplayName("Should generate display prefix with 12 characters")
    public void shouldGenerateDisplayPrefixWith12Characters() {
        // Given: User scope
        final ApiKeyScope scope = ApiKeyScope.USER;

        // When: Generating API key
        final GeneratedApiKey generatedKey = ApiKeyGenerator.generate(scope);

        // Then: Display prefix should be first 12 characters
        assertThat(generatedKey.getDisplayPrefix(), hasLength(DISPLAY_PREFIX_LENGTH));
        assertThat(generatedKey.getFullKey(), startsWith(generatedKey.getDisplayPrefix()));
    }

    @Test
    @DisplayName("Should generate unique keys on multiple calls")
    public void shouldGenerateUniqueKeysOnMultipleCalls() {
        // Given: User scope and multiple generations
        final ApiKeyScope scope = ApiKeyScope.USER;
        final int iterations = 100;
        final Set<String> generatedKeys = new HashSet<>();

        // When: Generating multiple API keys
        for (int i = 0; i < iterations; i++) {
            final GeneratedApiKey generatedKey = ApiKeyGenerator.generate(scope);
            generatedKeys.add(generatedKey.getFullKey());
        }

        // Then: All keys should be unique
        assertThat(generatedKeys, hasSize(iterations));
    }

    @Test
    @DisplayName("Should only contain lowercase alphanumeric after prefix")
    public void shouldOnlyContainLowercaseAlphanumericAfterPrefix() {
        // Given: User scope
        final ApiKeyScope scope = ApiKeyScope.USER;

        // When: Generating API key
        final GeneratedApiKey generatedKey = ApiKeyGenerator.generate(scope);
        final String keyAfterPrefix = generatedKey.getFullKey().substring(4); // Skip "evt_"

        // Then: Should only contain lowercase letters and digits
        assertThat(keyAfterPrefix, matchesRegex("[a-z0-9]{32}"));
    }
}
