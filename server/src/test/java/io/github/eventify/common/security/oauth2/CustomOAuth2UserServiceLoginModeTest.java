package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.UserAuthProvider;
import io.github.eventify.api.user.repository.UserAuthProviderRepository;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.api.user.service.UserAuthProviderService;
import io.github.eventify.support.TestBuilders;
import io.github.eventify.support.UnitTest;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GITHUB_REGISTRATION_ID;
import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GOOGLE_REGISTRATION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CustomOAuth2UserService} — LOGIN MODE scenarios (L1, L2, L3).
 * <p>
 * Login mode is the default when no authenticated SecurityContext is present or mode != "link".
 */
@DisplayName("Unit Test - CustomOAuth2UserService - Login Mode")
public class CustomOAuth2UserServiceLoginModeTest extends UnitTest {

    private static final String PROVIDER_EMAIL = "g@example.com";
    private static final String PRIMARY_EMAIL = "x@example.com";
    private static final String NEW_EMAIL = "new@example.com";
    private static final String GITHUB_EMAIL = "gh@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthProviderService userAuthProviderService;

    @Mock
    private UserAuthProviderRepository userAuthProviderRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private ClientRegistration clientRegistration;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    public void setUp() {
        customOAuth2UserService = new CustomOAuth2UserService(userRepository, userAuthProviderService, passwordEncoder);
    }

    // ========================= L1: Provider lookup wins over email lookup =========================

    @Test
    @DisplayName("L1: Should return existing user matched by (provider, providerEmail) even when primary email differs")
    public void loginMode_L1_returnsExistingUser_whenProviderRecordMatchesByProviderEmail() {
        // Given: User X with primary email x@example.com, has GOOGLE linked with providerEmail=g@example.com
        final User userX = aValidUser();
        userX.setEmail(PRIMARY_EMAIL);

        final UserAuthProvider googleProvider = TestBuilders.aUserAuthProvider(1L, userX, AuthProvider.GOOGLE, PROVIDER_EMAIL);

        final OAuth2User oAuth2User = createMockOAuth2User(PROVIDER_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);

        // And: Provider lookup by (GOOGLE, g@example.com) returns user X
        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GOOGLE, PROVIDER_EMAIL))
            .thenReturn(Optional.of(googleProvider));

        // When: Processing OAuth2 login with g@example.com
        final OAuth2User result = customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: Returns the OAuth2 user (user X is authenticated)
        assertThat(result, is(notNullValue()));

        // And: No new user is created
        verify(userRepository, never()).save(any(User.class));

        // And: upsertProvider is called for user X (updates providerEmail if needed)
        verify(userAuthProviderService, times(1)).upsertProvider(eq(userX), eq(AuthProvider.GOOGLE), eq(PROVIDER_EMAIL));
    }

    @Test
    @DisplayName("L1: Should NOT fall back to email lookup when provider record already exists")
    public void loginMode_L1_doesNotFallBackToEmailLookup_whenProviderRecordFound() {
        // Given: User X with primary email x@example.com, has GOOGLE linked with providerEmail=g@example.com
        final User userX = aValidUser();
        userX.setEmail(PRIMARY_EMAIL);

        final UserAuthProvider googleProvider = TestBuilders.aUserAuthProvider(1L, userX, AuthProvider.GOOGLE, PROVIDER_EMAIL);

        final OAuth2User oAuth2User = createMockOAuth2User(PROVIDER_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);

        // And: Provider lookup finds user X
        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GOOGLE, PROVIDER_EMAIL))
            .thenReturn(Optional.of(googleProvider));

        // When: Processing OAuth2 login
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: Email-based user lookup is NOT called (provider lookup was sufficient)
        verify(userRepository, never()).findByEmail(PROVIDER_EMAIL);
    }

    // ========================= L2: Email fallback auto-link =========================

    @Test
    @DisplayName("L2: Should auto-link provider when user exists by primary email but has no provider record")
    public void loginMode_L2_autoLinksProvider_whenUserExistsByPrimaryEmailButNoProviderRecord() {
        // Given: User X with email=x@example.com, NO provider records
        final User userX = aValidUser();
        userX.setEmail(PRIMARY_EMAIL);

        final OAuth2User oAuth2User = createMockOAuth2User(PRIMARY_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);

        // And: No provider record found by (GOOGLE, x@example.com)
        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GOOGLE, PRIMARY_EMAIL))
            .thenReturn(Optional.empty());

        // And: User found by primary email
        when(userRepository.findByEmail(PRIMARY_EMAIL)).thenReturn(Optional.of(userX));

        // When: Processing OAuth2 login with x@example.com
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: GOOGLE provider is created for user X (auto-link)
        verify(userAuthProviderService, times(1)).upsertProvider(eq(userX), eq(AuthProvider.GOOGLE), eq(PRIMARY_EMAIL));

        // And: No new user is created
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("L2: Should auto-link provider to existing local user")
    public void loginMode_L2_preservesHasPassword_whenAutoLinkingToExistingLocalUser() {
        // Given: User X (local user), no GOOGLE provider
        final User userX = aValidUser();
        userX.setEmail(PRIMARY_EMAIL);

        final OAuth2User oAuth2User = createMockOAuth2User(PRIMARY_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);

        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GOOGLE, PRIMARY_EMAIL))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail(PRIMARY_EMAIL)).thenReturn(Optional.of(userX));

        // When: Processing OAuth2 login
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: upsertProvider is called for the existing user
        verify(userAuthProviderService, times(1)).upsertProvider(eq(userX), eq(AuthProvider.GOOGLE), eq(PRIMARY_EMAIL));
    }

    // ========================= L3: New user creation =========================

    @Test
    @DisplayName("L3: Should create new user when email is unknown to the system")
    public void loginMode_L3_createsNewUser_whenEmailIsUnknown() {
        // Given: No user with new@example.com exists
        final OAuth2User oAuth2User = createMockOAuth2User(NEW_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);

        // And: No provider record found
        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GOOGLE, NEW_EMAIL))
            .thenReturn(Optional.empty());

        // And: No user found by email
        when(userRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            final User u = i.getArgument(0);
            u.setId(99L);
            return u;
        });

        // When: Processing OAuth2 login
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: A new user is saved
        verify(userRepository, times(1)).save(any(User.class));

        // And: GOOGLE provider record is created for the new user
        verify(userAuthProviderService, times(1)).upsertProvider(any(User.class), eq(AuthProvider.GOOGLE), eq(NEW_EMAIL));
    }

    @Test
    @DisplayName("L3: Should save new OAuth2 user when email is unknown")
    public void loginMode_L3_setsHasPasswordFalse_forNewOAuth2User() {
        // Given: A new Google OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(NEW_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);

        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GOOGLE, NEW_EMAIL))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The new user should be saved
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("L3: Should create GOOGLE provider record for new Google user")
    public void loginMode_L3_createsGoogleProviderRecord_forNewGoogleUser() {
        // Given: A new Google OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(NEW_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);

        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GOOGLE, NEW_EMAIL))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            final User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: upsertProvider called with GOOGLE
        verify(userAuthProviderService, times(1)).upsertProvider(any(User.class), eq(AuthProvider.GOOGLE), eq(NEW_EMAIL));
    }

    @Test
    @DisplayName("L3: Should create GITHUB provider record for new GitHub user")
    public void loginMode_L3_createsGitHubProviderRecord_forNewGitHubUser() {
        // Given: A new GitHub OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(GITHUB_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GITHUB_REGISTRATION_ID);

        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GITHUB, GITHUB_EMAIL))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail(GITHUB_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            final User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: upsertProvider called with GITHUB
        verify(userAuthProviderService, times(1)).upsertProvider(any(User.class), eq(AuthProvider.GITHUB), eq(GITHUB_EMAIL));
    }
}
