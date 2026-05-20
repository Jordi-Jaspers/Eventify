package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.notification.service.NotificationDispatchService;
import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.api.user.service.UserAuthProviderService;
import io.github.eventify.common.exception.OAuth2Exception;
import io.github.eventify.support.UnitTest;

import java.util.HashMap;
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
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GOOGLE_REGISTRATION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CustomOAuth2UserService#processOAuth2User(OAuth2UserRequest, OAuth2User)}.
 */
@DisplayName("Unit Test - CustomOAuth2UserService - Process OAuth2 User")
public class CustomOAuth2UserServiceProcessUserTest extends UnitTest {

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
        customOAuth2UserService = new CustomOAuth2UserService(
            userRepository,
            userAuthProviderService,
            passwordEncoder,
            mock(NotificationDispatchService.class)
        );
        lenient().when(userAuthProviderService.findByProviderAndProviderEmail(any(AuthProvider.class), any(String.class)))
            .thenReturn(Optional.empty());
    }

    @AfterEach
    public void tearDown() {
        OAuth2AttributesHolder.clear();
    }

    @Test
    @DisplayName("Should process new OAuth2 user successfully when user does not exist")
    public void shouldProcessNewOAuth2UserSuccessfullyWhenUserDoesNotExist() {
        // Given: A valid OAuth2 user that doesn't exist in the database
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            final User saved = i.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        // When: Processing the OAuth2 user
        final OAuth2User result = customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The result should be a non-null OAuth2User
        assertThat(result, is(notNullValue()));

        // And: A new user should be saved to the database
        verify(userRepository, times(1)).save(any(User.class));

        // And: The password encoder should be called once
        verify(passwordEncoder, times(1)).encode(any(String.class));

        // And: resolvedUserId is stashed in OAuth2AttributesHolder
        assertThat(OAuth2AttributesHolder.<Long>getAttribute("resolvedUserId"), is(99L));
    }

    @Test
    @DisplayName("Should process existing OAuth2 user successfully when user exists")
    public void shouldProcessExistingOAuth2UserSuccessfullyWhenUserExists() {
        // Given: An existing user in the database
        final User existingUser = aValidUser();
        existingUser.setId(42L);
        existingUser.setEmail(VALID_EMAIL);
        existingUser.setFirstName("");
        existingUser.setLastName("");

        // And: A valid OAuth2 user with verified email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        final OAuth2User result = customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The result should be a non-null OAuth2User
        assertThat(result, is(notNullValue()));

        // And: The user should be updated in the database
        verify(userRepository, times(1)).save(existingUser);

        // And: The password encoder should not be called
        verify(passwordEncoder, never()).encode(any(String.class));

        // And: The existing user's first and last names should be updated
        assertThat(existingUser.getFirstName(), is(equalTo(OAUTH2_FIRST_NAME)));
        assertThat(existingUser.getLastName(), is(equalTo(OAUTH2_LAST_NAME)));

        // And: resolvedUserId is stashed in OAuth2AttributesHolder
        assertThat(OAuth2AttributesHolder.<Long>getAttribute("resolvedUserId"), is(42L));
    }

    @Test
    @DisplayName("Should throw OAuth2Exception when email is null")
    public void shouldThrowOAuth2ExceptionWhenEmailIsNull() {
        // Given: An OAuth2 user with null email
        final OAuth2User oAuth2User = createMockOAuth2User(null, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);

        // When & Then: Processing should throw OAuth2Exception
        final OAuth2Exception exception = assertThrows(
            OAuth2Exception.class,
            () -> customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User)
        );

        // And: The exception message should indicate email is not available
        assertThat(exception.getMessage(), containsString("Email not publicly available from the OAuth2 provider or not verified."));

        // And: No user should be saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw OAuth2Exception when email is blank")
    public void shouldThrowOAuth2ExceptionWhenEmailIsBlank() {
        // Given: An OAuth2 user with blank email
        final OAuth2User oAuth2User = createMockOAuth2User("", true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);

        // When & Then: Processing should throw OAuth2Exception
        final OAuth2Exception exception = assertThrows(
            OAuth2Exception.class,
            () -> customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User)
        );

        // And: The exception message should indicate email is not available
        assertThat(exception.getMessage(), containsString("Email not publicly available from the OAuth2 provider or not verified."));

        // And: No user should be saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw OAuth2Exception when email is not verified")
    public void shouldThrowOAuth2ExceptionWhenEmailIsNotVerified() {
        // Given: An OAuth2 user with unverified email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, false);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);

        // When & Then: Processing should throw OAuth2Exception
        final OAuth2Exception exception = assertThrows(
            OAuth2Exception.class,
            () -> customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User)
        );

        // And: The exception message should indicate email is not verified
        assertThat(exception.getMessage(), containsString("Email not publicly available from the OAuth2 provider or not verified."));

        // And: No user should be saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw OAuth2Exception when provider is not supported")
    public void shouldThrowOAuth2ExceptionWhenProviderIsNotSupported() {
        // Given: An OAuth2 user from an unsupported provider
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", OAUTH2_USER_ID);
        attributes.put("email", VALID_EMAIL);
        attributes.put("email_verified", true);
        attributes.put("given_name", OAUTH2_FIRST_NAME);
        attributes.put("family_name", OAUTH2_LAST_NAME);

        final OAuth2User oAuth2User = new DefaultOAuth2User(
            null,
            attributes,
            "sub"
        );

        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(UNSUPPORTED_PROVIDER);

        // When & Then: Processing should throw OAuth2Exception
        final OAuth2Exception exception = assertThrows(
            OAuth2Exception.class,
            () -> customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User)
        );

        // And: The exception message should indicate provider is not supported
        assertThat(exception.getMessage(), containsString("Login with " + UNSUPPORTED_PROVIDER + " is not supported"));

        // And: No user should be saved
        verify(userRepository, never()).save(any(User.class));
    }
}
