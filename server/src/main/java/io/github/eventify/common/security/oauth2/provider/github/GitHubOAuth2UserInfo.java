package io.github.eventify.common.security.oauth2.provider.github;

import io.github.eventify.common.constant.Constants;
import io.github.eventify.common.security.oauth2.provider.OAuth2UserInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static io.github.jframe.datasource.search.model.SearchConstants.Character.SPACE;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * GitHub-specific implementation of OAuth2UserInfo. Extracts user information from GitHub's OAuth2 user attributes.
 */
@Getter
@RequiredArgsConstructor
public class GitHubOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return String.valueOf(attributes.get(Constants.OAuthAttributes.ID));
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
        return Optional.ofNullable((String) attributes.get(Constants.OAuthAttributes.NAME))
            .map(name -> name.split(SPACE, 2)[0])
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastName() {
        return Optional.ofNullable((String) attributes.get(Constants.OAuthAttributes.NAME))
            .map(name -> name.split(SPACE, 2))
            .filter(parts -> parts.length > 1)
            .map(parts -> parts[1])
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     *
     * GitHub doesn't provide an explicit email verification flag. We consider the email verified if GitHub provides it, as GitHub only
     * returns emails that users have verified and made public via their API.
     *
     * @return true if email is present (GitHub only returns verified emails), false otherwise.
     */
    @Override
    public boolean isEmailVerified() {
        return getEmail() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
