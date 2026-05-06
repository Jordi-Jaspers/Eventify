package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.UserAuthProvider;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.api.user.service.UserAuthProviderService;
import io.github.eventify.common.exception.LinkOAuth2Exception;
import io.github.eventify.support.TestBuilders;
import io.github.eventify.support.UnitTest;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static io.github.eventify.common.exception.ApiErrorCode.EMAIL_IN_USE_ERROR;
import static io.github.eventify.common.exception.ApiErrorCode.PROVIDER_ALREADY_LINKED_ERROR;
import static io.github.eventify.common.exception.ApiErrorCode.PROVIDER_LINKED_ELSEWHERE_ERROR;
import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GITHUB_REGISTRATION_ID;
import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GOOGLE_REGISTRATION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CustomOAuth2UserService} — LINK MODE scenarios (K1–K6).
 * <p>
 * Link mode is activated when mode=link is present in {@link OAuth2AttributesHolder} AND an authenticated user exists in SecurityContext.
 */
@DisplayName("Unit Test - CustomOAuth2UserService - Link Mode")
public class CustomOAuth2UserServiceLinkModeTest extends UnitTest {

    private static final String USER_X_EMAIL = "x@example.com";
    private static final String USER_Z_EMAIL = "z@example.com";
    private static final String PROVIDER_EMAIL_Y = "y@example.com";
    private static final String PROVIDER_EMAIL_G = "g@example.com";
    private static final String PROVIDER_EMAIL_GH = "gh@example.com";
    private static final String MODE_LINK = "link";

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
    }

    @AfterEach
    public void tearDown() {
        OAuth2AttributesHolder.clear();
    }

    // ========================= K1: Link with same email as current user =========================

    @Test
    @DisplayName("K1: Should link provider when OAuth2 email matches current user's primary email")
    public void linkMode_K1_linksProvider_whenOAuth2EmailMatchesCurrentUserEmail() {
        // Given: Authenticated user X with email=x@example.com, no GOOGLE link
        final User userX = aValidUser();
        userX.setId(1L);
        userX.setEmail(USER_X_EMAIL);

        final OAuth2User oAuth2User = createMockOAuth2User(USER_X_EMAIL, true);
        setupLinkModeRequest(GOOGLE_REGISTRATION_ID, MODE_LINK);

        // And: No existing provider record for (GOOGLE, x@example.com)
        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GOOGLE, USER_X_EMAIL))
            .thenReturn(Optional.empty());

        // And: No other user has x@example.com as primary email (other than userX)
        when(userRepository.findByEmail(USER_X_EMAIL)).thenReturn(Optional.of(userX));

        // When: Processing link mode with authenticated user X
        customOAuth2UserService.processLinkMode(oAuth2UserRequest, oAuth2User, userX);

        // Then: GOOGLE provider is created for user X with providerEmail=x@example.com
        verify(userAuthProviderService, times(1))
            .linkProviderForUser(eq(userX), eq(AuthProvider.GOOGLE), eq(USER_X_EMAIL));

        // And: resolvedUserId is stashed in OAuth2AttributesHolder
        assertThat(OAuth2AttributesHolder.<Long>getAttribute("resolvedUserId"), is(userX.getId()));
    }

    // ========================= K2: Link with different unknown email =========================

    @Test
    @DisplayName("K2: Should link provider when OAuth2 email is different and unknown to the system")
    public void linkMode_K2_linksProvider_whenOAuth2EmailIsDifferentAndUnknown() {
        // Given: Authenticated user X, no GOOGLE link
        final User userX = aValidUser();
        userX.setId(1L);
        userX.setEmail(USER_X_EMAIL);

        final OAuth2User oAuth2User = createMockOAuth2User(PROVIDER_EMAIL_Y, true);
        setupLinkModeRequest(GOOGLE_REGISTRATION_ID, MODE_LINK);

        // And: No provider record for (GOOGLE, y@example.com)
        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GOOGLE, PROVIDER_EMAIL_Y))
            .thenReturn(Optional.empty());

        // And: No user has y@example.com as primary email
        when(userRepository.findByEmail(PROVIDER_EMAIL_Y)).thenReturn(Optional.empty());

        // When: Processing link mode
        customOAuth2UserService.processLinkMode(oAuth2UserRequest, oAuth2User, userX);

        // Then: GOOGLE provider is created for user X with providerEmail=y@example.com
        verify(userAuthProviderService, times(1))
            .linkProviderForUser(eq(userX), eq(AuthProvider.GOOGLE), eq(PROVIDER_EMAIL_Y));

        // And: resolvedUserId is stashed in OAuth2AttributesHolder
        assertThat(OAuth2AttributesHolder.<Long>getAttribute("resolvedUserId"), is(userX.getId()));
    }

    // ========================= K3: Reject — email matches another user's primary email =========================

    @Test
    @DisplayName("K3: Should throw LinkOAuth2Exception with EMAIL_IN_USE_ERROR when OAuth2 email matches another user's primary email")
    public void linkMode_K3_throwsLinkOAuth2Exception_whenOAuth2EmailMatchesAnotherUsersPrimaryEmail() {
        // Given: Authenticated user X
        final User userX = aValidUser();
        userX.setId(1L);
        userX.setEmail(USER_X_EMAIL);

        // And: Another user Z exists with email=z@example.com
        final User userZ = aValidUser();
        userZ.setId(2L);
        userZ.setEmail(USER_Z_EMAIL);

        final OAuth2User oAuth2User = createMockOAuth2User(USER_Z_EMAIL, true);
        setupLinkModeRequest(GOOGLE_REGISTRATION_ID, MODE_LINK);

        // And: linkProviderForUser throws LinkOAuth2Exception with EMAIL_IN_USE_ERROR
        doThrow(new LinkOAuth2Exception(EMAIL_IN_USE_ERROR))
            .when(userAuthProviderService)
            .linkProviderForUser(eq(userX), eq(AuthProvider.GOOGLE), eq(USER_Z_EMAIL));

        // When & Then: Should throw LinkOAuth2Exception with EMAIL_IN_USE_ERROR
        final LinkOAuth2Exception exception = assertThrows(
            LinkOAuth2Exception.class,
            () -> customOAuth2UserService.processLinkMode(oAuth2UserRequest, oAuth2User, userX)
        );

        assertThat(exception.getErrorCode(), is(equalTo(EMAIL_IN_USE_ERROR)));

        // And: No provider record is created
        verify(userAuthProviderService, never()).upsertProvider(any(), any(), any());
    }

    // ========================= K4: Reject — provider email already linked to another user =========================

    @Test
    @DisplayName("K4: Should throw LinkOAuth2Exception with PROVIDER_LINKED_ELSEWHERE_ERROR when provider email is linked to another user")
    public void linkMode_K4_throwsLinkOAuth2Exception_whenProviderEmailAlreadyLinkedToAnotherUser() {
        // Given: Authenticated user X
        final User userX = aValidUser();
        userX.setId(1L);
        userX.setEmail(USER_X_EMAIL);

        // And: User Z has GOOGLE linked with providerEmail=g@example.com
        final User userZ = aValidUser();
        userZ.setId(2L);
        userZ.setEmail(USER_Z_EMAIL);

        final OAuth2User oAuth2User = createMockOAuth2User(PROVIDER_EMAIL_G, true);
        setupLinkModeRequest(GOOGLE_REGISTRATION_ID, MODE_LINK);

        // And: linkProviderForUser throws LinkOAuth2Exception with PROVIDER_LINKED_ELSEWHERE_ERROR
        doThrow(new LinkOAuth2Exception(PROVIDER_LINKED_ELSEWHERE_ERROR))
            .when(userAuthProviderService)
            .linkProviderForUser(eq(userX), eq(AuthProvider.GOOGLE), eq(PROVIDER_EMAIL_G));

        // When & Then: Should throw LinkOAuth2Exception with PROVIDER_LINKED_ELSEWHERE_ERROR
        final LinkOAuth2Exception exception = assertThrows(
            LinkOAuth2Exception.class,
            () -> customOAuth2UserService.processLinkMode(oAuth2UserRequest, oAuth2User, userX)
        );

        assertThat(exception.getErrorCode(), is(equalTo(PROVIDER_LINKED_ELSEWHERE_ERROR)));
    }

    // ========================= K5: Reject — same provider already linked to current user =========================

    @Test
    @DisplayName(
        "K5: Should throw LinkOAuth2Exception with PROVIDER_ALREADY_LINKED_ERROR when same provider already linked to current user"
    )
    public void linkMode_K5_throwsLinkOAuth2Exception_whenSameProviderAlreadyLinkedToCurrentUser() {
        // Given: Authenticated user X who already has GOOGLE linked
        final User userX = aValidUser();
        userX.setId(1L);
        userX.setEmail(USER_X_EMAIL);

        final OAuth2User oAuth2User = createMockOAuth2User(PROVIDER_EMAIL_G, true);
        setupLinkModeRequest(GOOGLE_REGISTRATION_ID, MODE_LINK);

        // And: linkProviderForUser throws LinkOAuth2Exception with PROVIDER_ALREADY_LINKED_ERROR
        doThrow(new LinkOAuth2Exception(PROVIDER_ALREADY_LINKED_ERROR))
            .when(userAuthProviderService)
            .linkProviderForUser(eq(userX), eq(AuthProvider.GOOGLE), eq(PROVIDER_EMAIL_G));

        // When & Then: Should throw LinkOAuth2Exception with PROVIDER_ALREADY_LINKED_ERROR
        final LinkOAuth2Exception exception = assertThrows(
            LinkOAuth2Exception.class,
            () -> customOAuth2UserService.processLinkMode(oAuth2UserRequest, oAuth2User, userX)
        );

        assertThat(exception.getErrorCode(), is(equalTo(PROVIDER_ALREADY_LINKED_ERROR)));
    }

    // ========================= K6: Link different provider while another is already linked =========================

    @Test
    @DisplayName("K6: Should link GitHub when Google is already linked to current user")
    public void linkMode_K6_linksGitHub_whenGoogleAlreadyLinkedToCurrentUser() {
        // Given: Authenticated user X who has GOOGLE linked
        final User userX = aValidUser();
        userX.setId(1L);
        userX.setEmail(USER_X_EMAIL);

        final UserAuthProvider googleProvider = TestBuilders.aUserAuthProvider(1L, userX, AuthProvider.GOOGLE, USER_X_EMAIL);

        final OAuth2User oAuth2User = createMockOAuth2User(PROVIDER_EMAIL_GH, true);
        setupLinkModeRequest(GITHUB_REGISTRATION_ID, MODE_LINK);

        // And: No GITHUB provider record for (GITHUB, gh@example.com)
        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GITHUB, PROVIDER_EMAIL_GH))
            .thenReturn(Optional.empty());

        // When: Processing link mode via GitHub
        customOAuth2UserService.processLinkMode(oAuth2UserRequest, oAuth2User, userX);

        // Then: GITHUB provider is created for user X
        verify(userAuthProviderService, times(1))
            .linkProviderForUser(eq(userX), eq(AuthProvider.GITHUB), eq(PROVIDER_EMAIL_GH));

        // And: resolvedUserId is stashed in OAuth2AttributesHolder
        assertThat(OAuth2AttributesHolder.<Long>getAttribute("resolvedUserId"), is(userX.getId()));

        // And: GOOGLE provider is NOT touched
        verify(userAuthProviderService, never()).upsertProvider(any(), eq(AuthProvider.GOOGLE), any());
    }

    // ========================= Security: mode=link without authenticated user falls back to login =========================

    @Test
    @DisplayName("Security: Should fall back to login mode when mode=link but no authenticated user in SecurityContext")
    public void linkMode_security_fallsBackToLoginMode_whenNoAuthenticatedUserInSecurityContext() {
        // Given: mode=link in request but NO authenticated user (null principal)
        final OAuth2User oAuth2User = createMockOAuth2User(USER_X_EMAIL, true);
        setupLinkModeRequest(GOOGLE_REGISTRATION_ID, MODE_LINK);

        when(userAuthProviderService.findByProviderAndProviderEmail(AuthProvider.GOOGLE, USER_X_EMAIL))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail(USER_X_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            final User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        // When: Processing OAuth2 user with mode=link but no authenticated user
        // (processOAuth2User should detect no SecurityContext and fall back to login)
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: Falls back to login mode — creates new user (no authenticated user to link to)
        verify(userRepository, times(1)).save(any(User.class));
        verify(userAuthProviderService, times(1)).upsertProvider(any(User.class), eq(AuthProvider.GOOGLE), eq(USER_X_EMAIL));
    }

    // ========================= Helper methods =========================

    private void setupLinkModeRequest(final String registrationId, final String mode) {
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(registrationId);
        OAuth2AttributesHolder.setAttributes(Map.of("mode", mode));
    }
}
