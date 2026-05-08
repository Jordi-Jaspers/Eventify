package io.github.eventify.common.util;

import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit Test - HashUtil")
public class HashUtilTest extends UnitTest {

    @Test
    @DisplayName("Should produce the same hash for the same input")
    public void shouldProduceSameHashForSameInput() {
        // Given: A token value
        final String tokenValue = "my-secret-refresh-token";

        // When: Hashing the same value twice
        final String firstHash = HashUtil.sha256(tokenValue);
        final String secondHash = HashUtil.sha256(tokenValue);

        // Then: Both hashes should be identical
        assertThat(firstHash, is(equalTo(secondHash)));
    }

    @Test
    @DisplayName("Should produce different hashes for different inputs")
    public void shouldProduceDifferentHashesForDifferentInputs() {
        // Given: Two different token values
        final String tokenA = "token-value-a";
        final String tokenB = "token-value-b";

        // When: Hashing each value
        final String hashA = HashUtil.sha256(tokenA);
        final String hashB = HashUtil.sha256(tokenB);

        // Then: The hashes should differ
        assertThat(hashA, is(not(equalTo(hashB))));
    }

    @Test
    @DisplayName("Should produce a 64-character hex string (SHA-256)")
    public void shouldProduceA64CharHexString() {
        // Given: Any token value
        final String tokenValue = "some-refresh-token-value";

        // When: Hashing the value
        final String hash = HashUtil.sha256(tokenValue);

        // Then: The result should be exactly 64 hex characters
        assertThat(hash, is(notNullValue()));
        assertThat(hash.length(), is(equalTo(64)));
        assertThat(hash, matchesPattern("[0-9a-f]{64}"));
    }

    @Test
    @DisplayName("Should throw when input is null")
    public void shouldThrowWhenInputIsNull() {
        // Given: A null input

        // When & Then: Hashing null should throw an exception
        assertThrows(Exception.class, () -> HashUtil.sha256(null));
    }

    @Test
    @DisplayName("Should produce a valid 64-char hash for an empty string")
    public void shouldProduceValidHashForEmptyString() {
        // Given: An empty string input
        final String emptyInput = "";

        // When: Hashing the empty string
        final String hash = HashUtil.sha256(emptyInput);

        // Then: Should still produce a valid 64-char hex hash (SHA-256 of empty string is deterministic)
        assertThat(hash, is(notNullValue()));
        assertThat(hash.length(), is(equalTo(64)));
        assertThat(hash, matchesPattern("[0-9a-f]{64}"));
    }
}
