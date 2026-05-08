package io.github.eventify.api.user.model;

/**
 * Enum representing the authentication providers supported by the application.
 */
public enum AuthProvider {

    LOCAL,
    GOOGLE,
    GITHUB;

    /**
     * Converts an OAuth2 registrationId (e.g. "google", "github") to the corresponding {@link AuthProvider}.
     *
     * @param registrationId the OAuth2 provider registration ID
     * @return the matching {@link AuthProvider}
     * @throws IllegalArgumentException if the registrationId is not supported
     */
    public static AuthProvider fromRegistrationId(final String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> GOOGLE;
            case "github" -> GITHUB;
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };
    }
}
