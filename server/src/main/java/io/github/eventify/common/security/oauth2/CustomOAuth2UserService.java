package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.notification.service.NotificationDispatchService;
import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.UserAuthProvider;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.api.user.service.UserAuthProviderService;
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

import static io.github.eventify.common.exception.ApiErrorCode.OAUTH2_EMAIL_NOT_AVAILABLE;
import static io.github.eventify.common.exception.ApiErrorCode.USER_ALREADY_EXISTS_ERROR;
import static io.github.eventify.common.security.oauth2.OAuth2Attributes.LINK_USER_ID;
import static io.github.eventify.common.security.oauth2.OAuth2Attributes.MODE;
import static io.github.eventify.common.security.oauth2.OAuth2Attributes.MODE_LINK;
import static io.github.eventify.common.security.oauth2.OAuth2Attributes.RESOLVED_USER_ID;
import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.getOAuth2UserInfo;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Custom OAuth2 user service that loads or creates users based on OAuth2 authentication. This service handles both existing users and new
 * user registration via OAuth2 providers, as well as linking OAuth2 providers to existing authenticated users.
 * <p>
 * The flow mode (login vs link) and link target user ID are read from {@link OAuth2AttributesHolder}, which is populated by
 * {@link OAuth2AttributesFilter} from the saved OAuth2 authorization request. The resolved user ID is then written back to the
 * holder under {@link OAuth2Attributes#RESOLVED_USER_ID} for use by {@link OAuth2AuthenticationSuccessHandler}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    private final UserAuthProviderService userAuthProviderService;

    private final PasswordEncoder passwordEncoder;

    private final NotificationDispatchService notificationDispatchService;

    /**
     * Loads the OAuth2 user from the provider and processes the authentication or linking flow.
     *
     * @param userRequest the OAuth2 user request containing client registration and access token
     * @return the authenticated {@link OAuth2User}
     * @throws OAuth2AuthenticationException if any error occurs during user loading or processing
     */
    @Override
    @Transactional
    public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            final OAuth2User oAuth2User = super.loadUser(userRequest);
            return processOAuth2User(userRequest, oAuth2User);
        } catch (final Exception exception) {
            final OAuth2Error errorCode = new OAuth2Error(exception.getMessage());
            throw new OAuth2AuthenticationException(errorCode, exception);
        }
    }

    /**
     * Processes the OAuth2 user after loading from the provider. Determines whether to run in login mode or link mode
     * based on the current {@link OAuth2AttributesHolder} state.
     *
     * @param userRequest the OAuth2 user request
     * @param oAuth2User  the raw OAuth2 user returned by the provider
     * @return the processed {@link OAuth2User}
     */
    public OAuth2User processOAuth2User(final OAuth2UserRequest userRequest, final OAuth2User oAuth2User) {
        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        final OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        requireVerifiedEmail(oAuth2UserInfo, registrationId);

        final String mode = OAuth2AttributesHolder.getAttribute(MODE);
        if (MODE_LINK.equals(mode)) {
            final User currentUser = getCurrentUserFromAttributes();
            if (currentUser != null) {
                return processLinkMode(userRequest, oAuth2User, currentUser);
            }
        }

        final AuthProvider authProvider = AuthProvider.fromRegistrationId(registrationId);
        final String email = oAuth2UserInfo.getEmail();
        final User user = resolveUserForLogin(authProvider, email, registrationId, oAuth2UserInfo);

        userAuthProviderService.upsertProvider(user, authProvider, email);
        if (user != null && user.getId() != null) {
            OAuth2AttributesHolder.setAttribute(RESOLVED_USER_ID, user.getId());
        }

        return oAuth2User;
    }

    /**
     * Processes the OAuth2 link mode flow, linking the provider account to the currently authenticated user.
     *
     * @param userRequest the OAuth2 user request
     * @param oAuth2User  the raw OAuth2 user returned by the provider
     * @param currentUser the currently authenticated user to link the provider to
     * @return the processed {@link OAuth2User}
     */
    public OAuth2User processLinkMode(final OAuth2UserRequest userRequest, final OAuth2User oAuth2User, final User currentUser) {
        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        final OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        requireVerifiedEmail(oAuth2UserInfo, registrationId);

        final AuthProvider authProvider = AuthProvider.fromRegistrationId(registrationId);
        final String providerEmail = oAuth2UserInfo.getEmail();

        userAuthProviderService.findByProviderAndProviderEmail(authProvider, providerEmail);
        userRepository.findByEmail(providerEmail);

        userAuthProviderService.linkProviderForUser(currentUser, authProvider, providerEmail);
        OAuth2AttributesHolder.setAttribute(RESOLVED_USER_ID, currentUser.getId());
        log.info("Link mode: linked provider '{}' with email '{}' for user '{}'", authProvider, providerEmail, currentUser.getEmail());

        return oAuth2User;
    }

    private User resolveUserForLogin(
        final AuthProvider authProvider,
        final String email,
        final String registrationId,
        final OAuth2UserInfo oAuth2UserInfo) {
        final Optional<UserAuthProvider> providerRecord =
            userAuthProviderService.findByProviderAndProviderEmail(authProvider, email);

        if (providerRecord.isPresent()) {
            log.debug("L1: Existing user found by provider record for OAuth2 email: {}", email);
            return providerRecord.get().getUser();
        }

        return resolveUserByEmailOrCreate(authProvider, email, registrationId, oAuth2UserInfo);
    }

    private User resolveUserByEmailOrCreate(
        final AuthProvider authProvider,
        final String email,
        final String registrationId,
        final OAuth2UserInfo oAuth2UserInfo) {
        final Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            log.debug("L2: Auto-linking provider '{}' to existing user with email: {}", authProvider, email);
            final User resolved = userByEmail.get();
            updateExistingUserIfNeeded(resolved, oAuth2UserInfo);
            return resolved;
        }

        log.debug("L3: Creating new user from OAuth2 provider: {}", registrationId);
        return createNewUser(oAuth2UserInfo);
    }

    private void requireVerifiedEmail(final OAuth2UserInfo oAuth2UserInfo, final String registrationId) {
        if (isBlank(oAuth2UserInfo.getEmail()) || !oAuth2UserInfo.isEmailVerified()) {
            log.debug("OAuth2 email not available or not verified. Provider: {}", registrationId);
            throw new OAuth2Exception(OAUTH2_EMAIL_NOT_AVAILABLE.getReason());
        }
    }

    private User createNewUser(final OAuth2UserInfo oAuth2UserInfo) {
        try {
            final User user = new User(oAuth2UserInfo);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            final User savedUser = userRepository.save(user);
            notificationDispatchService.dispatchWelcomeNotification(savedUser);
            return savedUser;
        } catch (final DataIntegrityViolationException exception) {
            log.debug("User already exists with email: {}", oAuth2UserInfo.getEmail());
            throw new OAuth2Exception(USER_ALREADY_EXISTS_ERROR.getReason(), exception);
        }
    }

    private void updateExistingUserIfNeeded(final User existingUser, final OAuth2UserInfo oAuth2UserInfo) {
        final boolean firstNameMissing = isBlank(existingUser.getFirstName());
        final boolean lastNameMissing = isBlank(existingUser.getLastName());

        if (firstNameMissing) {
            existingUser.setFirstName(oAuth2UserInfo.getFirstName());
        }
        if (lastNameMissing) {
            existingUser.setLastName(oAuth2UserInfo.getLastName());
        }
        if (firstNameMissing || lastNameMissing) {
            userRepository.save(existingUser);
        }
    }

    private User getCurrentUserFromAttributes() {
        final Object raw = OAuth2AttributesHolder.getAttribute(LINK_USER_ID);
        if (raw == null) {
            return null;
        }
        final Long userId = raw instanceof final Number n ? n.longValue() : Long.parseLong(raw.toString());
        return userRepository.findById(userId).orElse(null);
    }
}
