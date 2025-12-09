package io.github.eventify.common.security.oauth2.provider.google;

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
 * Unit Test - Google OAuth2 User Info
 */
@DisplayName("Unit Test - Google OAuth2 User Info")
public class GoogleOAuth2UserInfoTest extends UnitTest {

    private static final String UPPERCASE_EMAIL = "USER@EXAMPLE.COM";
    private static final String LOWERCASE_EMAIL = "user@example.com";
    private static final String BLANK_EMAIL = "   ";
    private static final String EMPTY_EMAIL = "";

    @Test
    @DisplayName("Should return ID from sub attribute")
    public void shouldReturnIdFromSubAttribute() {
        // Given: A Google OAuth2 attributes map with a sub value
        final Map<String, Object> attributes = aGoogleAttributes();
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Getting the ID
        final String actualId = userInfo.getId();

        // Then: The ID should match the sub attribute
        assertThat(actualId, is(notNullValue()));
        assertThat(actualId, is(equalTo(GOOGLE_USER_ID)));
    }

    @Test
    @DisplayName("Should return email in lowercase")
    public void shouldReturnEmailInLowercase() {
        // Given: A Google OAuth2 attributes map with an uppercase email
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, UPPERCASE_EMAIL);
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Getting the email
        final String actualEmail = userInfo.getEmail();

        // Then: The email should be returned in lowercase
        assertThat(actualEmail, is(notNullValue()));
        assertThat(actualEmail, is(equalTo(LOWERCASE_EMAIL)));
    }

    @Test
    @DisplayName("Should return null when email is blank")
    public void shouldReturnNullWhenEmailIsBlank() {
        // Given: A Google OAuth2 attributes map with a blank email
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, BLANK_EMAIL);
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Getting the email
        final String actualEmail = userInfo.getEmail();

        // Then: The email should be null
        assertThat(actualEmail, is(nullValue()));
    }

    @Test
    @DisplayName("Should return null when email is empty")
    public void shouldReturnNullWhenEmailIsEmpty() {
        // Given: A Google OAuth2 attributes map with an empty email
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, EMPTY_EMAIL);
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Getting the email
        final String actualEmail = userInfo.getEmail();

        // Then: The email should be null
        assertThat(actualEmail, is(nullValue()));
    }

    @Test
    @DisplayName("Should return null when email is null")
    public void shouldReturnNullWhenEmailIsNull() {
        // Given: A Google OAuth2 attributes map with a null email
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL, null);
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Getting the email
        final String actualEmail = userInfo.getEmail();

        // Then: The email should be null
        assertThat(actualEmail, is(nullValue()));
    }

    @Test
    @DisplayName("Should return first name from given_name attribute")
    public void shouldReturnFirstNameFromGivenNameAttribute() {
        // Given: A Google OAuth2 attributes map with a given_name value
        final Map<String, Object> attributes = aGoogleAttributes();
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Getting the first name
        final String actualFirstName = userInfo.getFirstName();

        // Then: The first name should match the given_name attribute
        assertThat(actualFirstName, is(notNullValue()));
        assertThat(actualFirstName, is(equalTo(GOOGLE_GIVEN_NAME)));
    }

    @Test
    @DisplayName("Should return null when given_name attribute is missing")
    public void shouldReturnNullWhenGivenNameAttributeIsMissing() {
        // Given: A Google OAuth2 attributes map without a given_name value
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.remove(Constants.OAuthAttributes.GIVEN_NAME);
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Getting the first name
        final String actualFirstName = userInfo.getFirstName();

        // Then: The first name should be null
        assertThat(actualFirstName, is(nullValue()));
    }

    @Test
    @DisplayName("Should return last name from family_name attribute")
    public void shouldReturnLastNameFromFamilyNameAttribute() {
        // Given: A Google OAuth2 attributes map with a family_name value
        final Map<String, Object> attributes = aGoogleAttributes();
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Getting the last name
        final String actualLastName = userInfo.getLastName();

        // Then: The last name should match the family_name attribute
        assertThat(actualLastName, is(notNullValue()));
        assertThat(actualLastName, is(equalTo(GOOGLE_FAMILY_NAME)));
    }

    @Test
    @DisplayName("Should return null when family_name attribute is missing")
    public void shouldReturnNullWhenFamilyNameAttributeIsMissing() {
        // Given: A Google OAuth2 attributes map without a family_name value
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.remove(Constants.OAuthAttributes.FAMILY_NAME);
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Getting the last name
        final String actualLastName = userInfo.getLastName();

        // Then: The last name should be null
        assertThat(actualLastName, is(nullValue()));
    }

    @Test
    @DisplayName("Should return true when email_verified attribute is true")
    public void shouldReturnTrueWhenEmailVerifiedAttributeIsTrue() {
        // Given: A Google OAuth2 attributes map with email_verified as true
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL_VERIFIED, true);
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Checking if email is verified
        final boolean isEmailVerified = userInfo.isEmailVerified();

        // Then: The result should be true
        assertThat(isEmailVerified, is(true));
    }

    @Test
    @DisplayName("Should return false when email_verified attribute is false")
    public void shouldReturnFalseWhenEmailVerifiedAttributeIsFalse() {
        // Given: A Google OAuth2 attributes map with email_verified as false
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL_VERIFIED, false);
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Checking if email is verified
        final boolean isEmailVerified = userInfo.isEmailVerified();

        // Then: The result should be false
        assertThat(isEmailVerified, is(false));
    }

    @Test
    @DisplayName("Should return false when email_verified attribute is missing")
    public void shouldReturnFalseWhenEmailVerifiedAttributeIsMissing() {
        // Given: A Google OAuth2 attributes map without email_verified attribute
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.remove(Constants.OAuthAttributes.EMAIL_VERIFIED);
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Checking if email is verified
        final boolean isEmailVerified = userInfo.isEmailVerified();

        // Then: The result should be false
        assertThat(isEmailVerified, is(false));
    }

    @Test
    @DisplayName("Should return false when email_verified attribute is null")
    public void shouldReturnFalseWhenEmailVerifiedAttributeIsNull() {
        // Given: A Google OAuth2 attributes map with email_verified as null
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL_VERIFIED, null);
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Checking if email is verified
        final boolean isEmailVerified = userInfo.isEmailVerified();

        // Then: The result should be false
        assertThat(isEmailVerified, is(false));
    }

    @Test
    @DisplayName("Should return false when email_verified attribute is not a boolean")
    public void shouldReturnFalseWhenEmailVerifiedAttributeIsNotBoolean() {
        // Given: A Google OAuth2 attributes map with email_verified as a string
        final Map<String, Object> attributes = aGoogleAttributes();
        attributes.put(Constants.OAuthAttributes.EMAIL_VERIFIED, "true");
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Checking if email is verified
        final boolean isEmailVerified = userInfo.isEmailVerified();

        // Then: The result should be false
        assertThat(isEmailVerified, is(false));
    }

    @Test
    @DisplayName("Should return raw attributes map")
    public void shouldReturnRawAttributesMap() {
        // Given: A Google OAuth2 attributes map
        final Map<String, Object> attributes = aGoogleAttributes();
        final GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // When: Getting the attributes
        final Map<String, Object> actualAttributes = userInfo.getAttributes();

        // Then: The attributes should match the original map
        assertThat(actualAttributes, is(notNullValue()));
        assertThat(actualAttributes, is(equalTo(attributes)));
    }
}
