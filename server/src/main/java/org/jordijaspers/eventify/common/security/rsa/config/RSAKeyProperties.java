package org.jordijaspers.eventify.common.security.rsa.config;

import lombok.Data;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.jordijaspers.eventify.common.security.rsa.RSAKeyGenerator;
import org.springframework.stereotype.Component;

/**
 * A class containing the public and private key for RSA encryption.
 */
@Data
@Component
public class RSAKeyProperties {

    private final RSAPrivateKey privateKey;

    private final RSAPublicKey publicKey;

    /**
     * Sets the private and public key with RSA encryption.
     */
    public RSAKeyProperties(final RSAKeyGenerator keyGenerator) {
        final KeyPair keyPair = keyGenerator.loadRsaKey();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }
}
