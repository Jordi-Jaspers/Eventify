package io.github.eventify.support;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.common.constant.Constants;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static java.time.ZoneOffset.UTC;

/**
 * Base class for all unit tests.
 * Contains common test constants and helper methods that can be reused across different test classes.
 */
@ExtendWith(MockitoExtension.class)
public class UnitTest {

    // Common Email Test Constants
    protected static final String VALID_EMAIL = "user@example.com";

    // Google OAuth2 Test Constants
    protected static final String GOOGLE_USER_ID = "google-user-12345";
    protected static final String GOOGLE_GIVEN_NAME = "John";
    protected static final String GOOGLE_FAMILY_NAME = "Doe";

    // GitHub OAuth2 Test Constants
    protected static final String GITHUB_USER_ID = "12345";
    protected static final String GITHUB_NAME = "John Doe";

    // Unsupported OAuth2 Provider
    protected static final String UNSUPPORTED_PROVIDER = "facebook";

    // Common Password Test Constants
    protected static final String VALID_PASSWORD = "Test123!@#";
    protected static final String VALID_PASSWORD_CONFIRMATION = "Test123!@#";
    protected static final String VALID_NEW_PASSWORD = "NewTest123!@#";
    protected static final String VALID_CONFIRM_PASSWORD = "NewTest123!@#";
    protected static final String VALID_PASSWORD_MIN_LENGTH = "Test123!";
    protected static final String WEAK_PASSWORD = "weak";
    protected static final String PASSWORD_TOO_SHORT = "Test1!";
    protected static final String PASSWORD_NO_UPPERCASE = "test123!@#";
    protected static final String PASSWORD_NO_LOWERCASE = "TEST123!@#";
    protected static final String PASSWORD_NO_DIGIT = "TestTest!@#";
    protected static final String PASSWORD_NO_SPECIAL = "Test123456";

    // OAuth2 Test Constants
    protected static final String OAUTH2_USER_ID = "123456789";
    protected static final String OAUTH2_FIRST_NAME = "John";
    protected static final String OAUTH2_LAST_NAME = "Doe";
    protected static final String ENCODED_PASSWORD = "encoded-password";
    protected static final String APPLICATION_URL = "http://localhost:3000";
    protected static final String ACCESS_TOKEN_VALUE = "access-token-value";
    protected static final String REFRESH_TOKEN_VALUE = "refresh-token-value";

    /**
     * Helper method to create a valid organization provisioning request.
     *
     * @param name The name of the organization.
     * @return A valid ProvisionOrganizationRequest object.
     */
    protected ProvisionOrganizationRequest aValidOrganizationRequest(final String name) {
        return new ProvisionOrganizationRequest()
            .setName(name)
            .setOwner(VALID_EMAIL);
    }

    /**
     * Helper method to create an organization provisioning request without owner (for testing owner validation).
     *
     * @param name The name of the organization.
     * @return A ProvisionOrganizationRequest object without owner.
     */
    protected ProvisionOrganizationRequest anOrganizationRequestWithoutOwner(final String name) {
        return new ProvisionOrganizationRequest().setName(name);
    }

    /**
     * Helper method to create a valid test user.
     *
     * @return A valid user with default values.
     */
    protected User aValidUser() {
        final User user = new User();
        user.setId(1L);
        user.setEmail(VALID_EMAIL);
        user.setFirstName(OAUTH2_FIRST_NAME);
        user.setLastName(OAUTH2_LAST_NAME);
        user.setPassword(ENCODED_PASSWORD);
        user.setEnabled(true);
        user.setValidated(true);
        user.setRole(Role.USER);
        return user;
    }

    /**
     * Helper method to create a valid test user with tokens.
     *
     * @return A valid user with access and refresh tokens.
     */
    protected User aValidUserWithTokens() {
        final User user = aValidUser();
        final Token accessToken = Token.builder()
            .value(ACCESS_TOKEN_VALUE)
            .type(TokenType.ACCESS_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusHours(1))
            .build();
        final Token refreshToken = Token.builder()
            .value(REFRESH_TOKEN_VALUE)
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .build();
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        return user;
    }

    /**
     * Helper method to create a mock OAuth2User with Google attributes.
     *
     * @param email         The email address.
     * @param emailVerified Whether the email is verified.
     * @return A mock OAuth2User with Google attributes.
     */
    protected OAuth2User createMockOAuth2User(final String email, final boolean emailVerified) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(Constants.OAuthAttributes.SUB, OAUTH2_USER_ID);
        attributes.put(Constants.OAuthAttributes.EMAIL, email);
        attributes.put(Constants.OAuthAttributes.EMAIL_VERIFIED, emailVerified);
        attributes.put(Constants.OAuthAttributes.GIVEN_NAME, OAUTH2_FIRST_NAME);
        attributes.put(Constants.OAuthAttributes.FAMILY_NAME, OAUTH2_LAST_NAME);

        return new DefaultOAuth2User(
            null,
            attributes,
            Constants.OAuthAttributes.SUB
        );
    }

    /**
     * Helper method to create a mock OAuth2User with Google attributes (email verified by default).
     *
     * @param email The email address.
     * @return A mock OAuth2User with Google attributes.
     */
    protected OAuth2User createMockOAuth2User(final String email) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(Constants.OAuthAttributes.SUB, OAUTH2_USER_ID);
        attributes.put(Constants.OAuthAttributes.EMAIL, email);

        return new DefaultOAuth2User(
            null,
            attributes,
            Constants.OAuthAttributes.SUB
        );
    }

    /**
     * Creates a valid Google OAuth2 attributes map with all required fields.
     *
     * @return A map containing valid Google OAuth2 user attributes.
     */
    protected Map<String, Object> aGoogleAttributes() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(Constants.OAuthAttributes.SUB, GOOGLE_USER_ID);
        attributes.put(Constants.OAuthAttributes.EMAIL, VALID_EMAIL);
        attributes.put(Constants.OAuthAttributes.GIVEN_NAME, GOOGLE_GIVEN_NAME);
        attributes.put(Constants.OAuthAttributes.FAMILY_NAME, GOOGLE_FAMILY_NAME);
        attributes.put(Constants.OAuthAttributes.EMAIL_VERIFIED, true);
        return attributes;
    }

    /**
     * Creates a valid GitHub OAuth2 attributes map with all required fields.
     *
     * @return A map containing valid GitHub OAuth2 user attributes.
     */
    protected Map<String, Object> aGitHubAttributes() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(Constants.OAuthAttributes.ID, Integer.parseInt(GITHUB_USER_ID));
        attributes.put(Constants.OAuthAttributes.EMAIL, VALID_EMAIL);
        attributes.put(Constants.OAuthAttributes.NAME, GITHUB_NAME);
        return attributes;
    }

    protected Channel aChannel(final Long id, final String name) {
        return TestBuilders.aChannel(id, name, aValidUser());
    }

    protected Channel aChannel(final Long id, final String name, final User user) {
        return TestBuilders.aChannel(id, name, user);
    }

    protected Channel aChannel(final Long id, final String name, final User user, final Organization org) {
        return TestBuilders.aChannel(id, name, user, org);
    }

    protected ApiKey anApiKey(final Long id, final String suffix, final String name, final User user) {
        return TestBuilders.anApiKey(id, suffix, name, user);
    }

    protected Watchlist aWatchlist(final Long id, final String name, final User user) {
        return TestBuilders.aWatchlist(id, name, user);
    }

    protected Watchlist anOrgWatchlist(final Long id, final String name, final User user, final Organization org) {
        return TestBuilders.anOrgWatchlist(id, name, user, org);
    }

    /**
     * Shadows Hamcrest's any(Class) to resolve static-import ambiguity with Mockito.
     * Delegates to Mockito's ArgumentMatchers.any(Class).
     */
    protected static <T> T any(final Class<T> cls) {
        return ArgumentMatchers.any(cls);
    }

    /**
     * Delegates to Mockito's ArgumentMatchers.any() (no-arg).
     */
    protected static <T> T any() {
        return ArgumentMatchers.any();
    }

}
