package io.github.eventify.common.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utility class for hashing values using SHA-256.
 */
@UtilityClass
public class HashUtil {

    /**
     * Hashes the given raw value using SHA-256 and returns a 64-character lowercase hex string.
     *
     * @param rawValue the raw value to hash
     * @return a 64-character lowercase hex string representing the SHA-256 hash
     * @throws NullPointerException if rawValue is null
     */
    public static String sha256(@NonNull final String rawValue) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashBytes = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
