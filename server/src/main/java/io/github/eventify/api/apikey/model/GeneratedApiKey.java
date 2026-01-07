package io.github.eventify.api.apikey.model;

import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Value object representing a generated API key with its full key and hashed version.
 */
@Getter
public class GeneratedApiKey {

    private final String fullKey;
    private final String displayPrefix;
    private final String hashedKey;

    /**
     * Constructor to create a GeneratedApiKey from a full key (for internal use without hashing).
     *
     * @param fullKey the full API key
     */
    public GeneratedApiKey(final String fullKey) {
        this.fullKey = fullKey;
        this.displayPrefix = fullKey.substring(0, 12);
        this.hashedKey = null;
    }

    /**
     * Constructor to create a GeneratedApiKey from a full key with hashing.
     *
     * @param fullKey the full API key
     * @param encoder the password encoder to hash the key
     */
    public GeneratedApiKey(final String fullKey, final PasswordEncoder encoder) {
        this.fullKey = fullKey;
        this.displayPrefix = fullKey.substring(0, 12);
        this.hashedKey = encoder.encode(fullKey);
    }
}
