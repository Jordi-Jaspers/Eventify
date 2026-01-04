package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.exception.OAuth2Exception;
import io.github.eventify.common.security.oauth2.provider.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.getOAuth2UserInfo;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Custom OAuth2 user service that loads or creates users based on OAuth2 authentication. This service handles both existing users and new
 * user registration via OAuth2 providers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * Load the user from the OAuth2 provider and create or update the user in the database.
     *
     * @param userRequest The OAuth2 user request.
     * @return The OAuth2 user.
     * @throws OAuth2AuthenticationException if authentication fails.
     */
    @Override
    @Transactional
    public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            return processOAuth2User(userRequest, super.loadUser(userRequest));
        } catch (final Exception exception) {
            final OAuth2Error errorCode = new OAuth2Error(exception.getMessage());
            throw new OAuth2AuthenticationException(errorCode, exception);
        }
    }

    /**
     * Process the OAuth2 user and create or update the user in the database.
     *
     * @param userRequest The OAuth2 user request.
     * @param oAuth2User  The OAuth2 user.
     * @return The OAuth2 user.
     */
    public OAuth2User processOAuth2User(final OAuth2UserRequest userRequest, final OAuth2User oAuth2User) {
        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        final OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        if (isBlank(oAuth2UserInfo.getEmail()) || !oAuth2UserInfo.isEmailVerified()) {
            throw new OAuth2Exception("Email not publicly available from " + registrationId + " or not verified.");
        }

        final Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            log.info("Existing user found for OAuth2 email: {}", oAuth2UserInfo.getEmail());
            updateExistingUser(user, oAuth2UserInfo);
        } else {
            log.info("Creating new user from OAuth2 provider: {}", registrationId);
            createNewUser(oAuth2UserInfo);
        }

        return oAuth2User;
    }

    /**
     * Create a new user from OAuth2 user info.
     * <p>
     * Note: OAuth2 users are assigned a random UUID password that is securely hashed. This means OAuth2 users cannot login via
     * password-based authentication without first performing a password reset. This is intentional for security - OAuth2 users should
     * authenticate via their OAuth2 provider or use the password reset flow to set a password for traditional login.
     *
     * @param oAuth2UserInfo The OAuth2 user info.
     */
    private void createNewUser(final OAuth2UserInfo oAuth2UserInfo) {
        try {
            final User user = new User(oAuth2UserInfo);
            final String password = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        } catch (final DataIntegrityViolationException exception) {
            throw new OAuth2Exception("A user with email " + oAuth2UserInfo.getEmail() + " already exists.", exception);
        }
    }

    /**
     * Update the user details if they are not already set.
     * <p>
     * Note: This method only fills in blank fields and does not overwrite existing values. This means if a user changes their name on the
     * OAuth2 provider (e.g., Google or GitHub), it will not be reflected in our system if they already have a name set. This is intentional
     * to allow users to maintain different display names in our system than on their OAuth2 provider.
     *
     * @param existingUser   The existing user.
     * @param oAuth2UserInfo The OAuth2 user info.
     */
    private void updateExistingUser(final User existingUser, final OAuth2UserInfo oAuth2UserInfo) {
        if (isBlank(existingUser.getFirstName())) {
            existingUser.setFirstName(oAuth2UserInfo.getFirstName());
        }

        if (isBlank(existingUser.getLastName())) {
            existingUser.setLastName(oAuth2UserInfo.getLastName());
        }

        userRepository.save(existingUser);
    }
}
