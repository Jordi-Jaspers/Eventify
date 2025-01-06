package org.jordijaspers.eventify.common.security.rsa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jordijaspers.eventify.common.exception.InternalServerException;
import org.jordijaspers.eventify.common.security.rsa.model.RSAKey;
import org.jordijaspers.eventify.common.security.rsa.repository.RSAKeyRepository;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * A service that generates and loads RSA keys in the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RSAKeyGenerator {

    private static final String RSA = "RSA";

    private final RSAKeyRepository rsaKeyRepository;

    /**
     * Load the RSA key from the database or generate a new one if none is found.
     *
     * @return The RSA key pair.
     */
    public KeyPair loadRsaKey() {
        log.info("Attempting to configure public and private keys for the application");
        return rsaKeyRepository.findAll()
            .stream()
            .findFirst()
            .map(this::toKeyPair)
            .orElseGet(this::generateRsaKey);
    }

    private KeyPair generateRsaKey() {
        log.debug("No RSA key found in the database, generating a new one..");
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(2048);
            return saveRsaKey(keyPairGenerator.generateKeyPair());
        } catch (final Exception exception) {
            log.error("Something went wrong while generating the RSA key");
            throw new InternalServerException(exception);
        }
    }

    private KeyPair toKeyPair(final RSAKey rsaKey) {
        log.debug("RSA key found in the database, loading it..");
        try {
            final PrivateKey privateKey = getPrivateKeyFromBytes(rsaKey.getPrivateKey());
            final PublicKey publicKey = getPublicKeyFromBytes(rsaKey.getPublicKey());
            return new KeyPair(publicKey, privateKey);
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException exception) {
            log.error("Something went wrong while loading the RSA key from the database", exception);
            return generateRsaKey();
        }
    }

    private KeyPair saveRsaKey(final KeyPair keyPair) {
        final RSAKey rsaKey = new RSAKey();
        rsaKey.setPrivateKey(keyPair.getPrivate().getEncoded());
        rsaKey.setPublicKey(keyPair.getPublic().getEncoded());
        rsaKeyRepository.save(rsaKey);
        return keyPair;
    }

    private PrivateKey getPrivateKeyFromBytes(final byte[] privateKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory.generatePrivate(keySpec);
    }

    private PublicKey getPublicKeyFromBytes(final byte[] publicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(keySpec);
    }
}
