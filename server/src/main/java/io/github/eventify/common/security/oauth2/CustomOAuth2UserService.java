package io.github.eventify.common.security.oauth2;

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

    private static final String EMAIL_ERROR_FORMAT = "Email not publicly available from %s or not verified.";

    private final UserRepository userRepository;

    private final UserAuthProviderService userAuthProviderService;

    private final PasswordEncoder passwordEncoder;

    /**
     * Loads the user from the OAuth2 provider, then routes to login or link processing based on the {@code mode} attribute.
     *
     * @param userRequest the OAuth2 user request
     * @return the OAuth2 user (login mode) or current authenticated user (link mode)
     * @throws OAuth2AuthenticationException if authentication fails
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
     * Processes an OAuth2 user, dispatching to link mode (when {@code mode=link} and a valid linking user is present)
     * or to login mode (L1/L2/L3 resolution). The resolved user's ID is written to {@link OAuth2AttributesHolder} under
     * {@link OAuth2Attributes#RESOLVED_USER_ID}.
     *
     * @param userRequest the OAuth2 user request
     * @param oAuth2User  the raw OAuth2 user from the provider
     * @return the OAuth2 user (passed through; user resolution side-effect is via the attributes holder)
     */
    public OAuth2User processOAuth2User(final OAuth2UserRequest userRequest, final OAuth2User oAuth2User) {
        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        final OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        requireVerifiedEmail(oAuth2UserInfo, registrationId);

        // Read mode from OAuth2AttributesHolder (populated by OAuth2AttributesFilter from session)
        final String mode = OAuth2AttributesHolder.getAttribute(MODE);
        if (MODE_LINK.equals(mode)) {
            final User currentUser = getCurrentUserFromAttributes();
            if (currentUser != null) {
                return processLinkMode(userRequest, oAuth2User, currentUser);
            }
            // No linkUserId or user not found → fall through to login mode (security fallback)
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
     * Processes the OAuth2 user in link mode (K1–K6).
     * <p>
     * Links the OAuth2 provider to the given authenticated user after performing cross-user safety checks
     * (delegated to {@link UserAuthProviderService#linkProviderForUser}).
     *
     * @param userRequest the OAuth2 user request
     * @param oAuth2User  the OAuth2 user
     * @param currentUser the currently authenticated user
     * @return the OAuth2 user (passed through)
     */
    public OAuth2User processLinkMode(final OAuth2UserRequest userRequest, final OAuth2User oAuth2User, final User currentUser) {
        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        final OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        requireVerifiedEmail(oAuth2UserInfo, registrationId);

        final AuthProvider authProvider = AuthProvider.fromRegistrationId(registrationId);
        final String providerEmail = oAuth2UserInfo.getEmail();

        // Pre-check: look up existing provider record and email owner for context
        userAuthProviderService.findByProviderAndProviderEmail(authProvider, providerEmail);
        userRepository.findByEmail(providerEmail);

        userAuthProviderService.linkProviderForUser(currentUser, authProvider, providerEmail);
        OAuth2AttributesHolder.setAttribute(RESOLVED_USER_ID, currentUser.getId());
        log.info("Link mode: linked provider '{}' with email '{}' for user '{}'", authProvider, providerEmail, currentUser.getEmail());

        return oAuth2User;
    }

    /**
     * Resolves the user for the login flow using the L1 → L2 → L3 strategy:
     * <ul>
     * <li>L1: lookup by (provider, providerEmail) — if a provider record exists, return its user</li>
     * <li>L2: fall back to email lookup — if found, auto-link the provider and preserve {@code hasPassword}</li>
     * <li>L3: otherwise, create a new user</li>
     * </ul>
     */
    private User resolveUserForLogin(
        final AuthProvider authProvider,
        final String email,
        final String registrationId,
        final OAuth2UserInfo oAuth2UserInfo) {
        final Optional<UserAuthProvider> providerRecord =
            userAuthProviderService.findByProviderAndProviderEmail(authProvider, email);

        if (providerRecord != null && providerRecord.isPresent()) {
            // L1: Provider record found — use that user directly
            log.info("L1: Existing user found by provider record for OAuth2 email: {}", email);
            return providerRecord.get().getUser();
        }

        // L2: Fall back to email lookup
        final Optional<User> userByEmail = userRepository.findByEmail(email);
        final User resolved;
        if (userByEmail.isPresent()) {
            // L2: User found by email — auto-link provider, preserve hasPassword
            resolved = userByEmail.get();
            log.info("L2: Auto-linking provider '{}' to existing user with email: {}", authProvider, email);
            updateExistingUserIfNeeded(resolved, oAuth2UserInfo);
        } else {
            // L3: Create new user
            log.info("L3: Creating new user from OAuth2 provider: {}", registrationId);
            resolved = createNewUser(oAuth2UserInfo);
        }
        return resolved;
    }

    /**
     * Validates that the OAuth2 provider returned a verified, non-blank email; otherwise throws.
     */
    private void requireVerifiedEmail(final OAuth2UserInfo oAuth2UserInfo, final String registrationId) {
        if (isBlank(oAuth2UserInfo.getEmail()) || !oAuth2UserInfo.isEmailVerified()) {
            throw new OAuth2Exception(String.format(EMAIL_ERROR_FORMAT, registrationId));
        }
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
    private User createNewUser(final OAuth2UserInfo oAuth2UserInfo) {
        try {
            final User user = new User(oAuth2UserInfo);
            user.setHasPassword(false);
            final String password = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(password));
            return userRepository.save(user);
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
    private void updateExistingUserIfNeeded(final User existingUser, final OAuth2UserInfo oAuth2UserInfo) {
        boolean changed = existingUser.isHasPassword();
        if (isBlank(existingUser.getFirstName())) {
            existingUser.setFirstName(oAuth2UserInfo.getFirstName());
            changed = true;
        }

        if (isBlank(existingUser.getLastName())) {
            existingUser.setLastName(oAuth2UserInfo.getLastName());
            changed = true;
        }

        if (changed) {
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
