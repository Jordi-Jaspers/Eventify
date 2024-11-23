package org.jordijaspers.eventify.common.security.rsa.model;

import lombok.Data;

import jakarta.persistence.*;

/**
 * A class containing the public and private key for RSA encryption.
 */
@Data
@Entity
@Table(name = "rsa_key")
public class RSAKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_key")
    private byte[] publicKey;

    @Column(name = "private_key")
    private byte[] privateKey;
}
