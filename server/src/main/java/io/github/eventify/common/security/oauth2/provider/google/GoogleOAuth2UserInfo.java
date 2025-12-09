package io.github.eventify.common.security.oauth2.provider.google;

import io.github.eventify.common.constant.Constants;
import io.github.eventify.common.security.oauth2.provider.OAuth2UserInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Google-specific implementation of OAuth2UserInfo. Extracts user information from Google's OAuth2 user attributes.
 */
@Getter
@RequiredArgsConstructor
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return (String) attributes.get(Constants.OAuthAttributes.SUB);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmail() {
        final String email = (String) attributes.get(Constants.OAuthAttributes.EMAIL);
        return isBlank(email) ? null : email.toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFirstName() {
        return (String) attributes.get(Constants.OAuthAttributes.GIVEN_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastName() {
        return (String) attributes.get(Constants.OAuthAttributes.FAMILY_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmailVerified() {
        final Object emailVerified = attributes.get(Constants.OAuthAttributes.EMAIL_VERIFIED);
        return Boolean.TRUE.equals(emailVerified);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
