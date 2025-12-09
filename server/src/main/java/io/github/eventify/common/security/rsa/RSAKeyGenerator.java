package io.github.eventify.common.security.rsa;

import io.github.eventify.common.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.*;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static io.github.eventify.common.constant.Constants.KeyGeneration.KEY_SIZE;
import static io.github.eventify.common.constant.Constants.KeyGeneration.RSA;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * A service that generates and loads RSA keys in the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RSAKeyGenerator {

    @Value("${security.rsa.seed}")
    private String seed;

    private Optional<KeyPair> cachedKeyPair = Optional.empty();

    /**
     * Load the RSA key from the database or generate a new one if none is found.
     *
     * @return The RSA key pair.
     */
    public KeyPair loadRsaKey() {
        log.info("Attempting to configure public and private keys for the application");
        if (cachedKeyPair.isEmpty()) {
            cachedKeyPair = Optional.of(generateRsaKey());
        }
        return cachedKeyPair.get();
    }

    private KeyPair generateRsaKey() {
        log.debug("No RSA key found in the cache, generating a new one..");
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            if (isBlank(seed)) {
                log.warn("No RSA seed configured. Using a random key that will change on restart.");
                keyPairGenerator.initialize(KEY_SIZE);
            } else {
                final SecureRandom secureRandom = createSeededSecureRandom(seed);
                keyPairGenerator.initialize(KEY_SIZE, secureRandom);
            }

            return keyPairGenerator.generateKeyPair();
        } catch (final Exception exception) {
            log.error("Something went wrong while generating the RSA key");
            throw new InternalServerException(exception);
        }
    }

    private SecureRandom createSeededSecureRandom(final String seed) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] seedBytes = digest.digest(seed.getBytes(UTF_8));

            final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(seedBytes);

            return secureRandom;
        } catch (final NoSuchAlgorithmException exception) {
            log.error("Failed to create seeded SecureRandom", exception);
            throw new InternalServerException(exception);
        }
    }
}
