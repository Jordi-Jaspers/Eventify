package org.jordijaspers.eventify.common.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.jordijaspers.eventify.api.user.service.UserService;
import org.jordijaspers.eventify.common.security.rsa.config.RSAKeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.jordijaspers.eventify.common.constants.Constants.Encoder.BCRYPT;

/**
 * The configuration for the authentication beans.
 */
@Configuration
public class AuthenticationConfig {

    /**
     * Indicates a class can process a specific {@link org.springframework.security.core.Authentication} implementation.
     *
     * @param userDetailsService the user details service to use
     * @param encoder            the password encoder to use
     * @return the authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider(final UserService userDetailsService, final PasswordEncoder encoder) {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(encoder);
        return authenticationProvider;
    }

    /**
     * Processes an {@link Authentication} request.
     *
     * @param configuration the authentication configuration
     * @return the authentication manager
     * @throws Exception when the authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * The password encoder to use.
     *
     * @return the BCrypt password encoder
     */
    @Bean
    @Primary
    public PasswordEncoder encoder() {
        final Map<String, PasswordEncoder> encoders = new ConcurrentHashMap<>();
        encoders.put(BCRYPT, new BCryptPasswordEncoder(10));
        return new DelegatingPasswordEncoder(BCRYPT, encoders);
    }

    /**
     * The JWT decoder for the public key.
     *
     * @param keys the RSA key properties
     * @return the JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder(final RSAKeyProperties keys) {
        return NimbusJwtDecoder.withPublicKey(keys.getPublicKey()).build();
    }

    /**
     * The JWT encoder for the private key.
     *
     * @param keys the RSA key properties
     * @return the JWT encoder
     */
    @Bean
    public JwtEncoder jwtEncoder(final RSAKeyProperties keys) {
        final JWK jwk = new RSAKey.Builder(keys.getPublicKey())
            .privateKey(keys.getPrivateKey())
            .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }
}
