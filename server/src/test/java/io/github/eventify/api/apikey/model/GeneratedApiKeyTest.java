package io.github.eventify.api.apikey.model;

import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test - Generated API Key.
 */
@DisplayName("Unit Test - Generated API Key")
public class GeneratedApiKeyTest extends UnitTest {

    private static final String FULL_KEY = "evt_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6";
    private static final String EXPECTED_DISPLAY_PREFIX = "evt_a1b2c3d4";
    private static final String HASHED_KEY = "hashed_key_value";

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_KEY);
    }

    @Test
    @DisplayName("Should extract display prefix from full key")
    public void shouldExtractDisplayPrefixFromFullKey() {
        // Given: A full API key
        final String fullKey = FULL_KEY;

        // When: Creating GeneratedApiKey
        final GeneratedApiKey generatedKey = new GeneratedApiKey(fullKey, passwordEncoder);

        // Then: Display prefix should be first 12 characters
        assertThat(generatedKey.getDisplayPrefix(), is(equalTo(EXPECTED_DISPLAY_PREFIX)));
        assertThat(generatedKey.getDisplayPrefix(), hasLength(12));
    }

    @Test
    @DisplayName("Should hash key using password encoder")
    public void shouldHashKeyUsingPasswordEncoder() {
        // Given: A full API key and password encoder
        final String fullKey = FULL_KEY;

        // When: Creating GeneratedApiKey
        final GeneratedApiKey generatedKey = new GeneratedApiKey(fullKey, passwordEncoder);

        // Then: Password encoder should be called with full key
        verify(passwordEncoder).encode(fullKey);

        // And: Hashed key should be stored
        assertThat(generatedKey.getHashedKey(), is(equalTo(HASHED_KEY)));
    }

    @Test
    @DisplayName("Should store full key for one time display")
    public void shouldStoreFullKeyForOneTimeDisplay() {
        // Given: A full API key
        final String fullKey = FULL_KEY;

        // When: Creating GeneratedApiKey
        final GeneratedApiKey generatedKey = new GeneratedApiKey(fullKey, passwordEncoder);

        // Then: Full key should be accessible
        assertThat(generatedKey.getFullKey(), is(equalTo(fullKey)));
        assertThat(generatedKey.getFullKey(), is(notNullValue()));
    }
}
