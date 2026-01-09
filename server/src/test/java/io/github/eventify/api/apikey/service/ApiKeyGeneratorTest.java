package io.github.eventify.api.apikey.service;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.model.GeneratedApiKey;
import io.github.eventify.support.UnitTest;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - API Key Generator.
 */
@DisplayName("Unit Test - API Key Generator")
public class ApiKeyGeneratorTest extends UnitTest {

    private static final int KEY_TOTAL_LENGTH = 36; // prefix(3) + underscore(1) + random(32)
    private static final int DISPLAY_PREFIX_LENGTH = 12;
    private static final String USER_PREFIX = "evt";
    private static final String ORG_PREFIX = "org";

    @Nested
    @DisplayName("Key Generation")
    class KeyGeneration {

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


    @Nested
    @DisplayName("Format Validation")
    class FormatValidation {

        @Test
        @DisplayName("Should return true for valid user key format")
        public void shouldReturnTrueForValidUserKeyFormat() {
            // Given: Valid user key
            final String key = "evt_abcdefghijklmnopqrstuvwxyz123456";

            // When: Validating format
            final boolean result = ApiKeyGenerator.isValidFormat(key);

            // Then: Should be valid
            assertThat(result, is(true));
        }

        @Test
        @DisplayName("Should return true for valid organization key format")
        public void shouldReturnTrueForValidOrgKeyFormat() {
            // Given: Valid org key
            final String key = "org_abcdefghijklmnopqrstuvwxyz123456";

            // When: Validating format
            final boolean result = ApiKeyGenerator.isValidFormat(key);

            // Then: Should be valid
            assertThat(result, is(true));
        }

        @Test
        @DisplayName("Should return false for null key")
        public void shouldReturnFalseForNullKey() {
            // Given: Null key

            // When: Validating format
            final boolean result = ApiKeyGenerator.isValidFormat(null);

            // Then: Should be invalid
            assertThat(result, is(false));
        }

        @Test
        @DisplayName("Should return false for key too short")
        public void shouldReturnFalseForKeyTooShort() {
            // Given: Key too short (less than 8 chars)
            final String key = "evt_abc";

            // When: Validating format
            final boolean result = ApiKeyGenerator.isValidFormat(key);

            // Then: Should be invalid
            assertThat(result, is(false));
        }

        @Test
        @DisplayName("Should return false for invalid prefix")
        public void shouldReturnFalseForInvalidPrefix() {
            // Given: Key with invalid prefix
            final String key = "xxx_abcdefghijklmnopqrstuvwxyz123456";

            // When: Validating format
            final boolean result = ApiKeyGenerator.isValidFormat(key);

            // Then: Should be invalid
            assertThat(result, is(false));
        }

        @Test
        @DisplayName("Should return false for key without underscore")
        public void shouldReturnFalseForKeyWithoutUnderscore() {
            // Given: Key without underscore after prefix
            final String key = "evtabcdefghijklmnopqrstuvwxyz123456";

            // When: Validating format
            final boolean result = ApiKeyGenerator.isValidFormat(key);

            // Then: Should be invalid
            assertThat(result, is(false));
        }
    }


    @Nested
    @DisplayName("Suffix Extraction")
    class SuffixExtraction {

        @Test
        @DisplayName("Should extract last 4 characters as suffix")
        public void shouldExtractLast4CharactersAsSuffix() {
            // Given: Valid key
            final String key = "evt_abcdefghijklmnopqrstuvwxyz1234";

            // When: Extracting suffix
            final String suffix = ApiKeyGenerator.extractSuffix(key);

            // Then: Should be last 4 chars
            assertThat(suffix, is("1234"));
        }

        @Test
        @DisplayName("Should throw exception for null key")
        public void shouldThrowExceptionForNullKey() {
            // Given: Null key

            // When/Then: Should throw exception
            assertThrows(IllegalArgumentException.class, () -> ApiKeyGenerator.extractSuffix(null));
        }

        @Test
        @DisplayName("Should throw exception for key too short")
        public void shouldThrowExceptionForKeyTooShort() {
            // Given: Key shorter than suffix length
            final String key = "abc";

            // When/Then: Should throw exception
            assertThrows(IllegalArgumentException.class, () -> ApiKeyGenerator.extractSuffix(key));
        }

        @Test
        @DisplayName("Should return correct suffix length constant")
        public void shouldReturnCorrectSuffixLengthConstant() {
            // When: Getting suffix length
            final int suffixLength = ApiKeyGenerator.getSuffixLength();

            // Then: Should be 4
            assertThat(suffixLength, is(4));
        }
    }
}
