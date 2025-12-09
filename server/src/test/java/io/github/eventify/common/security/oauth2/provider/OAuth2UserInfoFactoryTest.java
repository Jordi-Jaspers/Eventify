package io.github.eventify.common.security.oauth2.provider;

import io.github.eventify.common.exception.OAuth2Exception;
import io.github.eventify.common.security.oauth2.provider.github.GitHubOAuth2UserInfo;
import io.github.eventify.common.security.oauth2.provider.google.GoogleOAuth2UserInfo;
import io.github.eventify.support.UnitTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GITHUB_REGISTRATION_ID;
import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GOOGLE_REGISTRATION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - OAuth2UserInfoFactory
 */
@DisplayName("Unit Test - OAuth2UserInfoFactory")
public class OAuth2UserInfoFactoryTest extends UnitTest {

    private static final String TEST_USER_ID = "123456";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String UNSUPPORTED_PROVIDER = "facebook";

    @Test
    @DisplayName("Should create GoogleOAuth2UserInfo when provider is google in lowercase")
    public void shouldCreateGoogleOAuth2UserInfoWhenProviderIsGoogleInLowercase() {
        // Given: A map of Google OAuth2 user attributes
        final Map<String, Object> attributes = aGoogleUserAttributes();

        // When: Creating OAuth2UserInfo for google provider
        final OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(GOOGLE_REGISTRATION_ID, attributes);

        // Then: The returned instance should be GoogleOAuth2UserInfo
        assertThat(userInfo, is(notNullValue()));
        assertThat(userInfo, is(instanceOf(GoogleOAuth2UserInfo.class)));

        // And: The attributes should be correctly assigned
        assertThat(userInfo.getAttributes(), is(equalTo(attributes)));
    }

    @Test
    @DisplayName("Should create GoogleOAuth2UserInfo when provider is GOOGLE in uppercase")
    public void shouldCreateGoogleOAuth2UserInfoWhenProviderIsGoogleInUppercase() {
        // Given: A map of Google OAuth2 user attributes
        final Map<String, Object> attributes = aGoogleUserAttributes();

        // When: Creating OAuth2UserInfo for GOOGLE provider in uppercase
        final OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("GOOGLE", attributes);

        // Then: The returned instance should be GoogleOAuth2UserInfo
        assertThat(userInfo, is(notNullValue()));
        assertThat(userInfo, is(instanceOf(GoogleOAuth2UserInfo.class)));

        // And: The attributes should be correctly assigned
        assertThat(userInfo.getAttributes(), is(equalTo(attributes)));
    }

    @Test
    @DisplayName("Should create GoogleOAuth2UserInfo when provider is GoOgLe in mixed case")
    public void shouldCreateGoogleOAuth2UserInfoWhenProviderIsGoogleInMixedCase() {
        // Given: A map of Google OAuth2 user attributes
        final Map<String, Object> attributes = aGoogleUserAttributes();

        // When: Creating OAuth2UserInfo for GoOgLe provider in mixed case
        final OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("GoOgLe", attributes);

        // Then: The returned instance should be GoogleOAuth2UserInfo
        assertThat(userInfo, is(notNullValue()));
        assertThat(userInfo, is(instanceOf(GoogleOAuth2UserInfo.class)));

        // And: The attributes should be correctly assigned
        assertThat(userInfo.getAttributes(), is(equalTo(attributes)));
    }

    @Test
    @DisplayName("Should create GitHubOAuth2UserInfo when provider is github in lowercase")
    public void shouldCreateGitHubOAuth2UserInfoWhenProviderIsGithubInLowercase() {
        // Given: A map of GitHub OAuth2 user attributes
        final Map<String, Object> attributes = aGitHubUserAttributes();

        // When: Creating OAuth2UserInfo for github provider
        final OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(GITHUB_REGISTRATION_ID, attributes);

        // Then: The returned instance should be GitHubOAuth2UserInfo
        assertThat(userInfo, is(notNullValue()));
        assertThat(userInfo, is(instanceOf(GitHubOAuth2UserInfo.class)));

        // And: The attributes should be correctly assigned
        assertThat(userInfo.getAttributes(), is(equalTo(attributes)));
    }

    @Test
    @DisplayName("Should create GitHubOAuth2UserInfo when provider is GITHUB in uppercase")
    public void shouldCreateGitHubOAuth2UserInfoWhenProviderIsGithubInUppercase() {
        // Given: A map of GitHub OAuth2 user attributes
        final Map<String, Object> attributes = aGitHubUserAttributes();

        // When: Creating OAuth2UserInfo for GITHUB provider in uppercase
        final OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("GITHUB", attributes);

        // Then: The returned instance should be GitHubOAuth2UserInfo
        assertThat(userInfo, is(notNullValue()));
        assertThat(userInfo, is(instanceOf(GitHubOAuth2UserInfo.class)));

        // And: The attributes should be correctly assigned
        assertThat(userInfo.getAttributes(), is(equalTo(attributes)));
    }

    @Test
    @DisplayName("Should create GitHubOAuth2UserInfo when provider is GiThUb in mixed case")
    public void shouldCreateGitHubOAuth2UserInfoWhenProviderIsGithubInMixedCase() {
        // Given: A map of GitHub OAuth2 user attributes
        final Map<String, Object> attributes = aGitHubUserAttributes();

        // When: Creating OAuth2UserInfo for GiThUb provider in mixed case
        final OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("GiThUb", attributes);

        // Then: The returned instance should be GitHubOAuth2UserInfo
        assertThat(userInfo, is(notNullValue()));
        assertThat(userInfo, is(instanceOf(GitHubOAuth2UserInfo.class)));

        // And: The attributes should be correctly assigned
        assertThat(userInfo.getAttributes(), is(equalTo(attributes)));
    }

    @Test
    @DisplayName("Should throw OAuth2Exception when provider is unsupported")
    public void shouldThrowOAuth2ExceptionWhenProviderIsUnsupported() {
        // Given: A map of user attributes
        final Map<String, Object> attributes = aGoogleUserAttributes();

        // When & Then: Creating OAuth2UserInfo with unsupported provider should throw OAuth2Exception
        final OAuth2Exception exception = assertThrows(
            OAuth2Exception.class,
            () -> OAuth2UserInfoFactory.getOAuth2UserInfo(UNSUPPORTED_PROVIDER, attributes)
        );

        // And: The exception message should indicate the unsupported provider
        assertThat(exception.getMessage(), is(equalTo("Login with " + UNSUPPORTED_PROVIDER + " is not supported.")));
    }

    @Test
    @DisplayName("Should throw OAuth2Exception when provider is null")
    public void shouldThrowOAuth2ExceptionWhenProviderIsNull() {
        // Given: A map of user attributes
        final Map<String, Object> attributes = aGoogleUserAttributes();

        // When & Then: Creating OAuth2UserInfo with null provider should throw NullPointerException
        assertThrows(
            NullPointerException.class,
            () -> OAuth2UserInfoFactory.getOAuth2UserInfo(null, attributes)
        );
    }

    @Test
    @DisplayName("Should throw OAuth2Exception when provider is empty string")
    public void shouldThrowOAuth2ExceptionWhenProviderIsEmptyString() {
        // Given: A map of user attributes
        final Map<String, Object> attributes = aGoogleUserAttributes();

        // When & Then: Creating OAuth2UserInfo with empty provider should throw OAuth2Exception
        final OAuth2Exception exception = assertThrows(
            OAuth2Exception.class,
            () -> OAuth2UserInfoFactory.getOAuth2UserInfo("", attributes)
        );

        // And: The exception message should indicate the unsupported provider
        assertThat(exception.getMessage(), is(equalTo("Login with  is not supported.")));
    }

    @Test
    @DisplayName("Should create GoogleOAuth2UserInfo with empty attributes map")
    public void shouldCreateGoogleOAuth2UserInfoWithEmptyAttributesMap() {
        // Given: An empty attributes map
        final Map<String, Object> attributes = new HashMap<>();

        // When: Creating OAuth2UserInfo for google provider with empty attributes
        final OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(GOOGLE_REGISTRATION_ID, attributes);

        // Then: The returned instance should be GoogleOAuth2UserInfo
        assertThat(userInfo, is(notNullValue()));
        assertThat(userInfo, is(instanceOf(GoogleOAuth2UserInfo.class)));

        // And: The attributes should be the empty map
        assertThat(userInfo.getAttributes(), is(equalTo(attributes)));
    }

    @Test
    @DisplayName("Should create GitHubOAuth2UserInfo with empty attributes map")
    public void shouldCreateGitHubOAuth2UserInfoWithEmptyAttributesMap() {
        // Given: An empty attributes map
        final Map<String, Object> attributes = new HashMap<>();

        // When: Creating OAuth2UserInfo for github provider with empty attributes
        final OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(GITHUB_REGISTRATION_ID, attributes);

        // Then: The returned instance should be GitHubOAuth2UserInfo
        assertThat(userInfo, is(notNullValue()));
        assertThat(userInfo, is(instanceOf(GitHubOAuth2UserInfo.class)));

        // And: The attributes should be the empty map
        assertThat(userInfo.getAttributes(), is(equalTo(attributes)));
    }

    /**
     * Factory method to create a map of Google OAuth2 user attributes.
     *
     * @return A map containing Google OAuth2 user attributes.
     */
    private Map<String, Object> aGoogleUserAttributes() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", TEST_USER_ID);
        attributes.put("email", TEST_EMAIL);
        attributes.put("given_name", TEST_FIRST_NAME);
        attributes.put("family_name", TEST_LAST_NAME);
        attributes.put("email_verified", true);
        return attributes;
    }

    /**
     * Factory method to create a map of GitHub OAuth2 user attributes.
     *
     * @return A map containing GitHub OAuth2 user attributes.
     */
    private Map<String, Object> aGitHubUserAttributes() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", TEST_USER_ID);
        attributes.put("email", TEST_EMAIL);
        attributes.put("name", TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        return attributes;
    }
}
