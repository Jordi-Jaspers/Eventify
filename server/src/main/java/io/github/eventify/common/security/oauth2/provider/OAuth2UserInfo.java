package io.github.eventify.common.security.oauth2.provider;

import java.util.Map;

/**
 * Interface for extracting user information from OAuth2 providers.
 * Provides a common abstraction over different OAuth2 provider user attributes.
 */
public interface OAuth2UserInfo {

    /**
     * Get the provider's unique user ID.
     *
     * @return The provider-specific user ID.
     */
    String getId();

    /**
     * Get the user's email address.
     *
     * @return The user's email address.
     */
    String getEmail();

    /**
     * Get the user's first name.
     *
     * @return The user's first name.
     */
    String getFirstName();

    /**
     * Get the user's last name.
     *
     * @return The user's last name.
     */
    String getLastName();

    /**
     * Check if the user's email is verified.
     *
     * @return true if the email is verified, false otherwise.
     */
    boolean isEmailVerified();

    /**
     * Get the raw attributes from the OAuth2 provider.
     *
     * @return Map of all attributes from the provider.
     */
    Map<String, Object> getAttributes();
}
