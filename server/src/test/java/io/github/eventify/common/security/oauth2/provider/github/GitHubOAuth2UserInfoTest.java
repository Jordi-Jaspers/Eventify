package io.github.eventify.common.security.oauth2.provider.github;

import io.github.eventify.common.constant.Constants;
import io.github.eventify.support.UnitTest;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unit Test - GitHub OAuth2 User Info
 */
@DisplayName("Unit Test - GitHub OAuth2 User Info")
public class GitHubOAuth2UserInfoTest extends UnitTest {

    private static final String VALID_FULL_NAME = "John Doe";
    private static final String VALID_SINGLE_NAME = "John";
    private static final String VALID_MULTI_PART_NAME = "John Michael Doe";
    private static final String VALID_FIRST_NAME = "John";
    private static final String VALID_LAST_NAME_FROM_FULL = "Doe";
    private static final String VALID_LAST_NAME_FROM_MULTI = "Michael Doe";
    private static final String UPPERCASE_EMAIL = "USER@EXAMPLE.COM";
    private static final String LOWERCASE_EMAIL = "user@example.com";
    private static final String BLANK_EMAIL = "   ";
    private static final String EMPTY_EMAIL = "";

    @Test
    @DisplayName("Should return ID as string from id attribute")
    public void shouldReturnIdAsStringFromIdAttribute() {
        // Given: A GitHub OAuth2 attributes map with an integer ID
        final Map<String, Object> attributes = aGitHubAttributes();
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the ID
        final String actualId = userInfo.getId();

        // Then: The ID should be converted to a string
        assertThat(actualId, is(notNullValue()));
        assertThat(actualId, is(equalTo(GITHUB_USER_ID)));
    }

    @Test
    @DisplayName("Should return email in lowercase")
    public void shouldReturnEmailInLowercase() {
        // Given: A GitHub OAuth2 attributes map with an uppercase email
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, UPPERCASE_EMAIL);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the email
        final String actualEmail = userInfo.getEmail();

        // Then: The email should be returned in lowercase
        assertThat(actualEmail, is(notNullValue()));
        assertThat(actualEmail, is(equalTo(LOWERCASE_EMAIL)));
    }

    @Test
    @DisplayName("Should return null when email is blank")
    public void shouldReturnNullWhenEmailIsBlank() {
        // Given: A GitHub OAuth2 attributes map with a blank email
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, BLANK_EMAIL);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the email
        final String actualEmail = userInfo.getEmail();

        // Then: The email should be null
        assertThat(actualEmail, is(nullValue()));
    }

    @Test
    @DisplayName("Should return null when email is empty")
    public void shouldReturnNullWhenEmailIsEmpty() {
        // Given: A GitHub OAuth2 attributes map with an empty email
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, EMPTY_EMAIL);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the email
        final String actualEmail = userInfo.getEmail();

        // Then: The email should be null
        assertThat(actualEmail, is(nullValue()));
    }

    @Test
    @DisplayName("Should return null when email is null")
    public void shouldReturnNullWhenEmailIsNull() {
        // Given: A GitHub OAuth2 attributes map with a null email
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, null);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the email
        final String actualEmail = userInfo.getEmail();

        // Then: The email should be null
        assertThat(actualEmail, is(nullValue()));
    }

    @Test
    @DisplayName("Should return first name from single-word name")
    public void shouldReturnFirstNameFromSingleWordName() {
        // Given: A GitHub OAuth2 attributes map with a single-word name
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.NAME, VALID_SINGLE_NAME);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the first name
        final String actualFirstName = userInfo.getFirstName();

        // Then: The first name should be the entire name
        assertThat(actualFirstName, is(notNullValue()));
        assertThat(actualFirstName, is(equalTo(VALID_SINGLE_NAME)));
    }

    @Test
    @DisplayName("Should return first name from multi-word name")
    public void shouldReturnFirstNameFromMultiWordName() {
        // Given: A GitHub OAuth2 attributes map with a multi-word name
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.NAME, VALID_FULL_NAME);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the first name
        final String actualFirstName = userInfo.getFirstName();

        // Then: The first name should be the first part of the name
        assertThat(actualFirstName, is(notNullValue()));
        assertThat(actualFirstName, is(equalTo(VALID_FIRST_NAME)));
    }

    @Test
    @DisplayName("Should return first name from name with multiple spaces")
    public void shouldReturnFirstNameFromNameWithMultipleSpaces() {
        // Given: A GitHub OAuth2 attributes map with a name containing multiple spaces
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.NAME, VALID_MULTI_PART_NAME);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the first name
        final String actualFirstName = userInfo.getFirstName();

        // Then: The first name should be the first part before the first space
        assertThat(actualFirstName, is(notNullValue()));
        assertThat(actualFirstName, is(equalTo(VALID_FIRST_NAME)));
    }

    @Test
    @DisplayName("Should return null when name attribute is null")
    public void shouldReturnNullWhenNameAttributeIsNull() {
        // Given: A GitHub OAuth2 attributes map with a null name
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.NAME, null);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the first name
        final String actualFirstName = userInfo.getFirstName();

        // Then: The first name should be null
        assertThat(actualFirstName, is(nullValue()));
    }

    @Test
    @DisplayName("Should return null when name attribute is missing")
    public void shouldReturnNullWhenNameAttributeIsMissing() {
        // Given: A GitHub OAuth2 attributes map without a name attribute
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.remove(Constants.OAuthAttributes.NAME);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the first name
        final String actualFirstName = userInfo.getFirstName();

        // Then: The first name should be null
        assertThat(actualFirstName, is(nullValue()));
    }

    @Test
    @DisplayName("Should return last name from multi-word name")
    public void shouldReturnLastNameFromMultiWordName() {
        // Given: A GitHub OAuth2 attributes map with a multi-word name
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.NAME, VALID_FULL_NAME);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the last name
        final String actualLastName = userInfo.getLastName();

        // Then: The last name should be the remaining part after the first space
        assertThat(actualLastName, is(notNullValue()));
        assertThat(actualLastName, is(equalTo(VALID_LAST_NAME_FROM_FULL)));
    }

    @Test
    @DisplayName("Should return last name with multiple parts from name with multiple spaces")
    public void shouldReturnLastNameWithMultiplePartsFromNameWithMultipleSpaces() {
        // Given: A GitHub OAuth2 attributes map with a name containing multiple spaces
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.NAME, VALID_MULTI_PART_NAME);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the last name
        final String actualLastName = userInfo.getLastName();

        // Then: The last name should be everything after the first space
        assertThat(actualLastName, is(notNullValue()));
        assertThat(actualLastName, is(equalTo(VALID_LAST_NAME_FROM_MULTI)));
    }

    @Test
    @DisplayName("Should return null when name is single word")
    public void shouldReturnNullWhenNameIsSingleWord() {
        // Given: A GitHub OAuth2 attributes map with a single-word name
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.NAME, VALID_SINGLE_NAME);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the last name
        final String actualLastName = userInfo.getLastName();

        // Then: The last name should be null
        assertThat(actualLastName, is(nullValue()));
    }

    @Test
    @DisplayName("Should return null when name attribute is null")
    public void shouldReturnNullWhenNameAttributeIsNullForLastName() {
        // Given: A GitHub OAuth2 attributes map with a null name
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.NAME, null);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the last name
        final String actualLastName = userInfo.getLastName();

        // Then: The last name should be null
        assertThat(actualLastName, is(nullValue()));
    }

    @Test
    @DisplayName("Should return null when name attribute is missing for last name")
    public void shouldReturnNullWhenNameAttributeIsMissingForLastName() {
        // Given: A GitHub OAuth2 attributes map without a name attribute
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.remove(Constants.OAuthAttributes.NAME);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the last name
        final String actualLastName = userInfo.getLastName();

        // Then: The last name should be null
        assertThat(actualLastName, is(nullValue()));
    }

    @Test
    @DisplayName("Should return true when email is present")
    public void shouldReturnTrueWhenEmailIsPresent() {
        // Given: A GitHub OAuth2 attributes map with a valid email
        final Map<String, Object> attributes = aGitHubAttributes();
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Checking if email is verified
        final boolean isEmailVerified = userInfo.isEmailVerified();

        // Then: The result should be true
        assertThat(isEmailVerified, is(true));
    }

    @Test
    @DisplayName("Should return false when email is null")
    public void shouldReturnFalseWhenEmailIsNull() {
        // Given: A GitHub OAuth2 attributes map with a null email
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, null);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Checking if email is verified
        final boolean isEmailVerified = userInfo.isEmailVerified();

        // Then: The result should be false
        assertThat(isEmailVerified, is(false));
    }

    @Test
    @DisplayName("Should return false when email is blank")
    public void shouldReturnFalseWhenEmailIsBlank() {
        // Given: A GitHub OAuth2 attributes map with a blank email
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, BLANK_EMAIL);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Checking if email is verified
        final boolean isEmailVerified = userInfo.isEmailVerified();

        // Then: The result should be false
        assertThat(isEmailVerified, is(false));
    }

    @Test
    @DisplayName("Should return false when email is empty")
    public void shouldReturnFalseWhenEmailIsEmpty() {
        // Given: A GitHub OAuth2 attributes map with an empty email
        final Map<String, Object> attributes = aGitHubAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, EMPTY_EMAIL);
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Checking if email is verified
        final boolean isEmailVerified = userInfo.isEmailVerified();

        // Then: The result should be false
        assertThat(isEmailVerified, is(false));
    }

    @Test
    @DisplayName("Should return raw attributes map")
    public void shouldReturnRawAttributesMap() {
        // Given: A GitHub OAuth2 attributes map
        final Map<String, Object> attributes = aGitHubAttributes();
        final GitHubOAuth2UserInfo userInfo = new GitHubOAuth2UserInfo(attributes);

        // When: Getting the attributes
        final Map<String, Object> actualAttributes = userInfo.getAttributes();

        // Then: The attributes should match the original map
        assertThat(actualAttributes, is(notNullValue()));
        assertThat(actualAttributes, is(equalTo(attributes)));
    }
}
