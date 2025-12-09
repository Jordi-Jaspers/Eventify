package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.exception.OAuth2Exception;
import io.github.eventify.support.UnitTest;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GOOGLE_REGISTRATION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CustomOAuth2UserService} user creation logic.
 */
@DisplayName("Unit Test - CustomOAuth2UserService - Create New User")
public class CustomOAuth2UserServiceCreateUserTest extends UnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private ClientRegistration clientRegistration;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    public void setUp() {
        customOAuth2UserService = new CustomOAuth2UserService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should create new user successfully with encoded random password")
    public void shouldCreateNewUserSuccessfullyWithEncodedRandomPassword() {
        // Given: A valid OAuth2 user that doesn't exist in the database
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: A new user should be created and saved
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User savedUser = userCaptor.getValue();

        // And: The user should have the correct email
        assertThat(savedUser.getEmail(), is(equalTo(VALID_EMAIL)));

        // And: The user should have the correct first name
        assertThat(savedUser.getFirstName(), is(equalTo(OAUTH2_FIRST_NAME)));

        // And: The user should have the correct last name
        assertThat(savedUser.getLastName(), is(equalTo(OAUTH2_LAST_NAME)));

        // And: The user should have an encoded password
        assertThat(savedUser.getPassword(), is(equalTo(ENCODED_PASSWORD)));

        // And: The user should be enabled
        assertThat(savedUser.isEnabled(), is(true));

        // And: The user should be validated
        assertThat(savedUser.isValidated(), is(true));

        // And: The user should have USER role
        assertThat(savedUser.getRole(), is(equalTo(Role.USER)));
    }

    @Test
    @DisplayName("Should encode password with random UUID when creating new user")
    public void shouldEncodePasswordWithRandomUuidWhenCreatingNewUser() {
        // Given: A valid OAuth2 user that doesn't exist in the database
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The password encoder should be called with a UUID string
        final ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder, times(1)).encode(passwordCaptor.capture());

        final String capturedPassword = passwordCaptor.getValue();

        // And: The password should be a non-empty string (UUID)
        assertThat(capturedPassword, is(notNullValue()));
        assertThat(capturedPassword.length(), greaterThan(0));
    }

    @Test
    @DisplayName("Should throw OAuth2Exception when DataIntegrityViolationException occurs")
    public void shouldThrowOAuth2ExceptionWhenDataIntegrityViolationExceptionOccurs() {
        // Given: A valid OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);

        // And: The repository throws DataIntegrityViolationException (duplicate email)
        when(userRepository.save(any(User.class)))
            .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        // When & Then: Processing should throw OAuth2Exception
        final OAuth2Exception exception = assertThrows(
            OAuth2Exception.class,
            () -> customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User)
        );

        // And: The exception message should indicate duplicate email
        assertThat(exception.getMessage(), containsString("A user with email " + VALID_EMAIL + " already exists"));
    }

    @Test
    @DisplayName("Should create user with all OAuth2 attributes populated")
    public void shouldCreateUserWithAllOAuth2AttributesPopulated() {
        // Given: A valid OAuth2 user with all attributes
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The saved user should have all OAuth2 info
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User savedUser = userCaptor.getValue();

        // And: All fields should be populated correctly
        assertThat(savedUser.getEmail(), is(equalTo(VALID_EMAIL)));
        assertThat(savedUser.getFirstName(), is(equalTo(OAUTH2_FIRST_NAME)));
        assertThat(savedUser.getLastName(), is(equalTo(OAUTH2_LAST_NAME)));
        assertThat(savedUser.getPassword(), is(notNullValue()));
        assertThat(savedUser.isEnabled(), is(true));
        assertThat(savedUser.isValidated(), is(true));
        assertThat(savedUser.getRole(), is(equalTo(Role.USER)));
    }

    @Test
    @DisplayName("Should create user with enabled flag set to true")
    public void shouldCreateUserWithEnabledFlagSetToTrue() {
        // Given: A valid OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The created user should be enabled
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User savedUser = userCaptor.getValue();

        // And: The enabled flag should be true
        assertThat(savedUser.isEnabled(), is(true));
    }

    @Test
    @DisplayName("Should create user with validated flag set to true")
    public void shouldCreateUserWithValidatedFlagSetToTrue() {
        // Given: A valid OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The created user should be validated
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User savedUser = userCaptor.getValue();

        // And: The validated flag should be true
        assertThat(savedUser.isValidated(), is(true));
    }
}
