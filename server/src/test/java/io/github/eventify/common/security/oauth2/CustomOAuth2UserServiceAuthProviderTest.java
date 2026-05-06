package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.api.user.service.UserAuthProviderService;
import io.github.eventify.support.UnitTest;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
 * Unit tests for {@link CustomOAuth2UserService} — UserAuthProvider upsert logic.
 */
@DisplayName("Unit Test - CustomOAuth2UserService - UserAuthProvider Upsert")
public class CustomOAuth2UserServiceAuthProviderTest extends UnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthProviderService userAuthProviderService;

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
        lenient().when(userAuthProviderService.findByProviderAndProviderEmail(any(AuthProvider.class), any(String.class)))
            .thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Should upsert UserAuthProvider with GOOGLE provider for new Google user")
    public void processOAuth2User_createsUserAuthProviderRecord_forNewGoogleUser() {
        // Given: A new Google OAuth2 user that doesn't exist in the database
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            final User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: upsertProvider should be called with GOOGLE provider and the user's email
        verify(userAuthProviderService, times(1)).upsertProvider(any(User.class), eq(AuthProvider.GOOGLE), eq(VALID_EMAIL));
    }

    @Test
    @DisplayName("Should upsert UserAuthProvider with GITHUB provider for new GitHub user")
    public void processOAuth2User_createsUserAuthProviderRecord_forNewGitHubUser() {
        // Given: A new GitHub OAuth2 user that doesn't exist in the database
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GITHUB_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            final User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: upsertProvider should be called with GITHUB provider and the user's email
        verify(userAuthProviderService, times(1)).upsertProvider(any(User.class), eq(AuthProvider.GITHUB), eq(VALID_EMAIL));
    }

    @Test
    @DisplayName("Should upsert UserAuthProvider for existing user on OAuth2 login")
    public void processOAuth2User_upsertsUserAuthProviderRecord_forExistingUser() {
        // Given: An existing user logging in via Google
        final User existingUser = aValidUser();
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: upsertProvider should be called once for the existing user
        verify(userAuthProviderService, times(1)).upsertProvider(eq(existingUser), eq(AuthProvider.GOOGLE), eq(VALID_EMAIL));
    }

    @Test
    @DisplayName("Should delegate dedupe to UserAuthProviderService on second login with same provider")
    public void processOAuth2User_doesNotCreateDuplicateProvider_onSecondLogin() {
        // Given: An existing user that already has GOOGLE linked (dedupe is the service's responsibility)
        final User existingUser = aValidUser();
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user a second time
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: upsertProvider is called exactly once — dedupe is delegated to the service
        verify(userAuthProviderService, times(1)).upsertProvider(eq(existingUser), eq(AuthProvider.GOOGLE), eq(VALID_EMAIL));
    }

    @Test
    @DisplayName("Should set hasPassword=false when creating new OAuth2 user")
    public void processOAuth2User_setsHasPasswordFalse_forNewOAuth2User() {
        // Given: A new Google OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The created user should have hasPassword=false
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User savedUser = userCaptor.getValue();

        // And: hasPassword should be false (OAuth2 users don't have a real password)
        assertThat(savedUser.isHasPassword(), is(false));
    }

    @Test
    @DisplayName("Should link Google provider to existing local user without changing hasPassword")
    public void processOAuth2User_linksGoogleProvider_whenExistingLocalUserAuthenticatesViaGoogle() {
        // Given: An existing local user (hasPassword=true) with no Google provider linked
        final User existingUser = aValidUser();
        existingUser.setHasPassword(true);

        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: upsertProvider should be called with GOOGLE for the existing user
        verify(userAuthProviderService, times(1)).upsertProvider(eq(existingUser), eq(AuthProvider.GOOGLE), eq(VALID_EMAIL));

        // And: hasPassword must remain true — linking a provider must not flip it
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();
        assertThat(savedUser.isHasPassword(), is(true));
    }

    @Test
    @DisplayName("Should link GitHub provider to existing Google user without changing hasPassword")
    public void processOAuth2User_linksGitHubProvider_whenExistingGoogleUserAuthenticatesViaGitHub() {
        // Given: An existing OAuth2 user (hasPassword=false, originally Google) linking GitHub
        final User existingUser = aValidUser();
        existingUser.setHasPassword(false);

        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GITHUB_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: upsertProvider should be called with GITHUB for the existing user
        verify(userAuthProviderService, times(1)).upsertProvider(eq(existingUser), eq(AuthProvider.GITHUB), eq(VALID_EMAIL));

        // And: hasPassword must remain false — linking a provider must not flip it
        assertThat(existingUser.isHasPassword(), is(false));
    }
}
