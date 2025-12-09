package io.github.eventify.common.security.oauth2.provider;

import io.github.eventify.common.exception.OAuth2Exception;
import io.github.eventify.common.security.oauth2.provider.github.GitHubOAuth2UserInfo;
import io.github.eventify.common.security.oauth2.provider.google.GoogleOAuth2UserInfo;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * Factory for creating OAuth2UserInfo instances based on the provider.
 */
@UtilityClass
public final class OAuth2UserInfoFactory {

    public static final String GOOGLE_REGISTRATION_ID = "google";

    public static final String GITHUB_REGISTRATION_ID = "github";

    /**
     * Create an OAuth2UserInfo instance based on the registration ID (provider).
     *
     * @param registrationId The OAuth2 provider registration ID (e.g., "Google", "GitHub").
     * @param attributes     The user attributes from the OAuth2 provider.
     * @return An OAuth2UserInfo instance for the specified provider.
     * @throws OAuth2Exception if the provider is not supported.
     */
    public static OAuth2UserInfo getOAuth2UserInfo(final String registrationId, final Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case GOOGLE_REGISTRATION_ID -> new GoogleOAuth2UserInfo(attributes);
            case GITHUB_REGISTRATION_ID -> new GitHubOAuth2UserInfo(attributes);
            default -> throw new OAuth2Exception("Login with " + registrationId + " is not supported.");
        };
    }
}
